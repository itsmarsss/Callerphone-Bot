package com.itsmarsss.callerphone.call.model;

/**
 * How two parties were linked.
 * {@link #HOURLY_SCHEDULE} is reserved for availability-based matching.
 */
public enum CallMatchSource {
    /** Classic cross-server live queue (guild text channels). */
    LIVE_QUEUE,
    /** Future: weekly availability → hourly random DM pair. */
    HOURLY_SCHEDULE
}
