package com.itsmarsss.callerphone.call.schedule;

import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.model.CallMatchSource;
import com.itsmarsss.callerphone.call.model.CallSession;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Stub for scheduled hourly matching.
 * When implemented: load slots for current day/hour, pair users at random,
 * open CallSessions with {@link CallMatchSource#HOURLY_SCHEDULE} (likely DM endpoints).
 */
public final class HourlyMatchPlanner {

    public Optional<CallSession> tryBuildPair(List<AvailabilitySlot> pool, ZonedDateTime nowUtc) {
        // Not live yet — architecture hook only
        return Optional.empty();
    }

    public CallSession openScheduledSession(CallEndpoint a, CallEndpoint b) {
        return new CallSession(a, b, CallMatchSource.HOURLY_SCHEDULE);
    }

    public Instant nextFireTime(ZonedDateTime nowUtc) {
        return nowUtc.plusHours(1).withMinute(0).withSecond(0).withNano(0).toInstant();
    }
}
