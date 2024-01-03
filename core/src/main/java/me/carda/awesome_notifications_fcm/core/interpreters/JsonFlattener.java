package me.carda.awesome_notifications_fcm.core.interpreters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class JsonFlattener {

    private static boolean isNumber(String value){
        return value != null && value.matches("\\d+");
    }

    @NonNull
    private static Object getStructure(@NonNull String key, @NonNull String nextKey, @NonNull Object collection){
        Object nextCollection = null;
        if (collection instanceof List) {
            List<Object> list = (List<Object>) collection;
            int index = Integer.parseInt(key);
            if (index >= 0 && index < list.size()) {
                nextCollection = list.get(index);
            }
        }
        else if(collection instanceof Map) {
            nextCollection = ((Map<String, Object>) collection).get(key);
        }

        if (nextCollection == null) {
            if (isNumber(nextKey)) return new ArrayList<>();
            return new HashMap<>();
        }
        return nextCollection;
    }

    private static void putStructure(String key, Object value, Object structure){
        if (structure instanceof List){
            if (!isNumber(key)) return;
            addToList(Integer.parseInt(key), value, (List<Object>) structure);
        }
        if (structure instanceof Map){
            addToMap(key, value, (Map<String, Object>) structure);
        }
    }

    private static void addToList(Integer index, Object value, List<Object> list) {
        while (list.size() <= index) {
            list.add(null); // Adding null as a placeholder
        }
        list.set(index, value);
    }

    private static void addToMap(String key, Object value, Map<String, Object> map) {
        map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    private static void addToStructure(
            @NonNull Map<String, Object> root,
            @NonNull String key,
            @Nullable String value
    ) {
        String[] keys = key.split("\\.");
        Object lastStructure = root;

        if (keys.length == 1) {
            root.put(key, convertToProperValue(value));
            return;
        }

        int lastIndex = keys.length - 1;
        for (int position = 0; position < lastIndex; position++) {
            String currentKey = keys[position];
            String nextKey = keys[position+1];
            Object currentStructure = getStructure(currentKey, nextKey, lastStructure);
            putStructure(currentKey, currentStructure, lastStructure);
            lastStructure = currentStructure;
        }

        String lastPart = keys[lastIndex];
        putStructure(lastPart, convertToProperValue(value), lastStructure);
    }

    private static Object convertToProperValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(value);
            }
            return value;
        }
    }

    public static void removeNullValues(Map<String, Object> map) {
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            Object value = entry.getValue();
            if (value == null) {
                // Remove entry if value is null
                iterator.remove();
            } else if (value instanceof Map) {
                // Recursively remove nulls from nested maps
                removeNullValues((Map<String, Object>) value);
            } else if (value instanceof List) {
                // Recursively remove nulls from lists
                removeNullValuesFromList((List<Object>) value);
            }
        }
    }

    private static void removeNullValuesFromList(List<Object> list) {
        Iterator<Object> iterator = list.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element == null) {
                iterator.remove();
            } else if (element instanceof Map) {
                removeNullValues((Map<String, Object>) element);
            } else if (element instanceof List) {
                removeNullValuesFromList((List<Object>) element);
            }
        }
    }

    @NonNull
    public static Map<String, Object> decode(Map<String, String> flatMap) {
        Map<String, Object> unflattenedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : flatMap.entrySet()) {
            addToStructure(unflattenedMap, entry.getKey(), entry.getValue());
        }

        removeNullValues(unflattenedMap);
        return unflattenedMap;
    }
}
