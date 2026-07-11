package com.itsmarsss.database;

import org.bson.Document;

import java.util.Collections;
import java.util.List;

public final class DatabaseUtil {
    private DatabaseUtil() {
    }

    public static String getOrDefault(Document document, String key, String defaultValue) {
        if (document == null || !document.containsKey(key) || document.get(key) == null) {
            return defaultValue;
        }
        Object value = document.get(key);
        return value instanceof String ? (String) value : String.valueOf(value);
    }

    public static long getOrDefault(Document document, String key, long defaultValue) {
        if (document == null || !document.containsKey(key) || document.get(key) == null) {
            return defaultValue;
        }
        Object value = document.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int getOrDefaultInt(Document document, String key, int defaultValue) {
        if (document == null || !document.containsKey(key) || document.get(key) == null) {
            return defaultValue;
        }
        Object value = document.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getOrDefault(Document document, String key, boolean defaultValue) {
        if (document == null || !document.containsKey(key) || document.get(key) == null) {
            return defaultValue;
        }
        Object value = document.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getOrDefault(Document document, String key, List<T> defaultValue, Class<T> className) {
        if (document == null || !document.containsKey(key) || document.get(key) == null) {
            return defaultValue != null ? defaultValue : Collections.<T>emptyList();
        }
        List<T> list = document.getList(key, className);
        return list != null ? list : (defaultValue != null ? defaultValue : Collections.<T>emptyList());
    }
}
