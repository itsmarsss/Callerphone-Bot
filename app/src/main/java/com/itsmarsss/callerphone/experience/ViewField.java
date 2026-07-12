package com.itsmarsss.callerphone.experience;

public record ViewField(String name, String value, boolean inline) {
    public static ViewField of(String name, String value) {
        return new ViewField(name, value, true);
    }

    public static ViewField block(String name, String value) {
        return new ViewField(name, value, false);
    }
}
