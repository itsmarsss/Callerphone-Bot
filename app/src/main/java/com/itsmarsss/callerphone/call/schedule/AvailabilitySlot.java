package com.itsmarsss.callerphone.call.schedule;

import java.time.DayOfWeek;

/**
 * Future hourly matching: user is available on one weekday at a UTC hour.
 * Example: FRIDAY at 20 → every Friday 20:00 UTC they enter the hourly pool.
 * Product rule (planned): pick 1 day/week, hours they can take a random link.
 */
public record AvailabilitySlot(
        String userId,
        DayOfWeek dayOfWeek,
        int hourUtc,
        boolean enabled
) {
    public AvailabilitySlot {
        if (hourUtc < 0 || hourUtc > 23) {
            throw new IllegalArgumentException("hourUtc must be 0-23");
        }
    }
}
