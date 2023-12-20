package me.carda.awesome_notifications_fcm.core.interpreters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class JsonFlattenerTest {
    @Test
    public void simpleUnflattenerTest() {
        Map<String, String> flatMap = new HashMap<>();

        flatMap.put("content.id", "1");
        flatMap.put("content.badge", "50");
        flatMap.put("content.channelKey", "alerts");
        flatMap.put("content.displayOnForeground", "true");
        flatMap.put("content.notificationLayout", "BigPicture");
        flatMap.put("content.largeIcon", "https://example.com/large-icon.jpg");
        flatMap.put("content.bigPicture", "https://example.com/big-picture.jpg");
        flatMap.put("content.showWhen", "true");
        flatMap.put("content.autoDismissible", "true");
        flatMap.put("content.privacy", "Private");
        flatMap.put("content.payload.secret", "Awesome Notifications Rocks!");
        flatMap.put("actionButtons.0.key", "REDIRECT");
        flatMap.put("actionButtons.0.label", "Redirect");
        flatMap.put("actionButtons.0.autoDismissible", "true");
        flatMap.put("actionButtons.1.key", "DISMISS");
        flatMap.put("actionButtons.1.label", "Dismiss");
        flatMap.put("actionButtons.1.actionType", "DismissAction");
        flatMap.put("actionButtons.1.isDangerousOption", "true");
        flatMap.put("actionButtons.1.autoDismissible", "true");

        Map<String, Object> unflattenedMap = JsonFlattener.decode(flatMap);

        // Assertions for 'content' map
        assertTrue(unflattenedMap.containsKey("content"));
        Map<String, Object> content = (Map<String, Object>) unflattenedMap.get("content");
        assertEquals(1, content.get("id"));
        assertEquals(50, content.get("badge"));
        assertEquals("alerts", content.get("channelKey"));
        assertEquals(true, content.get("displayOnForeground"));
        assertEquals("BigPicture", content.get("notificationLayout"));
        assertEquals("https://example.com/large-icon.jpg", content.get("largeIcon"));
        assertEquals("https://example.com/big-picture.jpg", content.get("bigPicture"));
        assertEquals(true, content.get("showWhen"));
        assertEquals(true, content.get("autoDismissible"));
        assertEquals("Private", content.get("privacy"));

        // Assertions for 'payload' inside 'content'
        Map<String, String> payload = (Map<String, String>) content.get("payload");
        assertEquals("Awesome Notifications Rocks!", payload.get("secret"));

        // Assertions for 'actionButtons' list
        assertTrue(unflattenedMap.containsKey("actionButtons"));
        List<Object> actionButtons = (List<Object>) unflattenedMap.get("actionButtons");

        // Assertions for each action button
        Map<String, Object> button1 = (Map<String, Object>) actionButtons.get(0);
        assertEquals("REDIRECT", button1.get("key"));
        assertEquals("Redirect", button1.get("label"));
        assertEquals(true, button1.get("autoDismissible"));

        Map<String, Object> button2 = (Map<String, Object>) actionButtons.get(1);
        assertEquals("DISMISS", button2.get("key"));
        assertEquals("Dismiss", button2.get("label"));
        assertEquals("DismissAction", button2.get("actionType"));
        assertEquals(true, button2.get("isDangerousOption"));
        assertEquals(true, button2.get("autoDismissible"));

    }
}