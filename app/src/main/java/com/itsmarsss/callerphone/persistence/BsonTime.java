package com.itsmarsss.callerphone.persistence;

import java.time.Instant;
import java.util.Date;

public final class BsonTime {
    private BsonTime() {
    }

    public static Date toDate(Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    public static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    public static Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date date) {
            return date.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof String s && !s.isBlank()) {
            return Instant.parse(s);
        }
        return null;
    }
}
