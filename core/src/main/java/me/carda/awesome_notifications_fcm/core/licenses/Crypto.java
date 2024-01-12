package me.carda.awesome_notifications_fcm.core.licenses;

import androidx.annotation.Nullable;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

final class Crypto {

    static final String keyType = "RSA";
    static final String signProtocol = "SHA256with"+keyType;

    static final String publicKey =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAojIclHvIDZtrISYJT1yu" +
        "bine2wDALTCnxf96q08RAc1tfkgbXYnd3ILqa3viOmKD6hNALqWzjmIY+yotB21y" +
        "SJEsDAiLB8SVDG6ai+Lcp3pQ6YmO76YzmgGdFZoN/1t7iEhn82f75PgxAKmBoIc5" +
        "9gF2MB9JZgiMW0arsz3oFiqhw/YEK6wp5fU54wLwr0Xp38BiONnWharZxgrmAnAD" +
        "ozwH8t5Hq21NMbwRwF+yUGhKYzQOIUoIRl9DRMSdw3eQ/o74pJb5GeIkko37Z1Ec" +
        "px2xEZ7OjVFM9S8ZGipHPeJxFogyWZcFSzJ83AvbQH/dioXY0Bj3+J0GnPbe2Sgx" +
        "dwIDAQAB";

    @Nullable
    static PublicKey getPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        return KeyFactory.getInstance(keyType).generatePublic(
            new X509EncodedKeySpec(Encoder.decodeBase64(publicKey))
        );
    }
}
