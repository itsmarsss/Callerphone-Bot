package com.marsss.database;

import org.bson.Document;

import java.util.List;

public class DatabaseUtil {
    public static String getOrDefault(Document document, String key, String defaultValue) {
        return document.containsKey(key) ? document.getString(key) : defaultValue;
    }

    public static long getOrDefault(Document document, String key, long defaultValue) {
        return document.containsKey(key) ? document.getLong(key) : defaultValue;
    }

    public static boolean getOrDefault(Document document, String key, boolean defaultValue) {
        return document.containsKey(key) ? document.getBoolean(key) : defaultValue;
    }

    public static <T> List<T> getOrDefault(Document document, String key, List defaultValue, Class className) {
        return document.containsKey(key) ? document.getList(key, className) : defaultValue;
    }
}
