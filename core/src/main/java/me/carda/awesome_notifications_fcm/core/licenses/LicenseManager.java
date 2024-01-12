package me.carda.awesome_notifications_fcm.core.licenses;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.List;
import java.util.Objects;

import me.carda.awesome_notifications.core.AwesomeNotifications;
import me.carda.awesome_notifications.core.exceptions.AwesomeNotificationsException;
import me.carda.awesome_notifications.core.logs.Logger;
import me.carda.awesome_notifications.core.utils.StringUtils;
import me.carda.awesome_notifications_fcm.core.managers.FcmDefaultsManager;

enum LicenseErrorState {
    expired,
    singleDoNotMatch,
    withoutValidation
}

public final class LicenseManager {
    public final String TAG = "LicenseManager";
    public final String LIB_VERSION = "0.9.1";
    public final int LIB_DATE = 20240111;

    public static LicenseErrorState licenseErrorState = LicenseErrorState.withoutValidation;

    // ************** SINGLETON PATTERN ***********************

    private static LicenseManager instance;

    protected LicenseManager(){}

    public static LicenseManager getInstance() {
        if(instance == null)
            instance = new LicenseManager();
        return instance;
    }

    // ********************************************************

    public boolean isLicenseKeyValid(
        @NonNull Context context
    ) throws AwesomeNotificationsException
    {
        List<String> licenseKeys = FcmDefaultsManager.getLicenseKeys(context);
        if (licenseKeys == null) return false;

        try {
            PublicKey publicKey = Crypto.getPublicKey();
            if(publicKey == null) return false;

            for (String licenseKey : licenseKeys){
                if(StringUtils.getInstance().isNullOrEmpty(licenseKey)) {
                    return false;
                }

                String[] parts = licenseKey.split("==", 2);
                String prefix = parts[0];
                String base64Encoded = parts[1];

                // License keys from year 1
                if ("".equals(base64Encoded)) {
                    licenseErrorState = LicenseErrorState.expired;
                    continue;
                }
                boolean isSingleVersion = prefix.startsWith("single::");

                if(isSingleVersion){
                    if(!licenseKey.startsWith("single::"+ LIB_VERSION +"==")){
                        licenseErrorState = LicenseErrorState.singleDoNotMatch;
                        continue;
                    }
                    prefix = prefix
                            .replaceFirst("single::", "");
                }

                if(
                    assignerVerify(
                        AwesomeNotifications.getPackageName(context),
                        prefix,
                        publicKey,
                        Base64.decode(base64Encoded, Base64.DEFAULT))
                ){
                    if (isSingleVersion) {
                        if (LIB_VERSION.equals(prefix)) return true;
                        licenseErrorState = LicenseErrorState.singleDoNotMatch;
                    } else {
                        int licenseDate = Integer.parseInt(prefix.replace("-", ""));
                        if (LIB_DATE <= licenseDate + 10000) return true;
                        licenseErrorState = LicenseErrorState.expired;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @NonNull
    private boolean assignerVerify(
            @NonNull String packageName,
            @NonNull String signaturePrefix,
            @NonNull PublicKey publicKey,
            @NonNull byte[] signature
    ) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        Signature publicSign = Signature.getInstance(Crypto.signProtocol);
        publicSign.initVerify(publicKey);
        publicSign.update((signaturePrefix+":"+packageName)
                .getBytes(StandardCharsets.UTF_8));
        return publicSign.verify(signature);
    }

    public boolean printValidationTest(
            @NonNull Context context
    ) throws AwesomeNotificationsException {
        if(!LicenseManager
                .getInstance()
                .isLicenseKeyValid(context)) {
            printLicenseMessageError(context);
            return false;
        }
        else {
            Logger.d(TAG, "Awesome FCM License key validated");
            return true;
        }
    }

    void printLicenseMessageError(Context context) {
        String licenseMessage;
        switch (licenseErrorState){
            case expired:
                licenseMessage =
                        "WARNING: The current licenses for Awesome Notifications does not cover this FCM plugin release. " +
                                "Please update your license to use the latest version of Awesome Notification's FCM plugin in release mode without watermarks. " +
                                "Application ID: \"" + AwesomeNotifications.getPackageName(context) + "\". Version: "+ LIB_VERSION +" . "+
                                "For more information and to update your license, please visit https://awesome-notifications.carda.me#prices.";
                break;
            case singleDoNotMatch:
                licenseMessage =
                        "WARNING: Your current single license key does not cover this version of the Awesome Notifications FCM plugin. " +
                                "Please upgrade your license to use this version of the plugin in release mode without limitations. " +
                                "Application ID: \"" + AwesomeNotifications.getPackageName(context) + "\". Version: "+ LIB_VERSION +" . "+
                                "For more information and to upgrade your license, please visit https://awesome-notifications.carda.me#prices.";
                break;
            case withoutValidation:
            default:
                licenseMessage =
                        "You need to insert a valid license key (Year 2) to use Awesome Notification's FCM " +
                                "plugin in release mode without watermarks (application id: \"" +
                                AwesomeNotifications.getPackageName(context) +
                                "\"). Version: "+ LIB_VERSION +" . To know more about it, please " +
                                "visit https://awesome-notifications.carda.me#prices";
                break;
        }


        boolean isDebuggable = false;
        try {
            isDebuggable = ( 0 != (
                    context
                            .getPackageManager()
                            .getApplicationInfo(
                                    AwesomeNotifications.getPackageName(context),
                                    ApplicationInfo.FLAG_DEBUGGABLE)
                            .flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        switch (licenseErrorState){
            case expired:
            case singleDoNotMatch:
                Logger.w(TAG, licenseMessage);
                break;

            case withoutValidation:
            default:
                if(isDebuggable) {
                    Logger.i(TAG, licenseMessage);
                } else {
                    Logger.e(TAG, licenseMessage);
                }
                break;
        }
    }
}
