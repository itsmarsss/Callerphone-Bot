package com.itsmarsss.callerphone.call.model;

import java.time.Duration;
import java.time.Instant;

public final class CallQueueEntry {
    private final CallEndpoint endpoint;
    private final Instant enqueuedAt;
    private final CallMatchSource source;

    public CallQueueEntry(CallEndpoint endpoint, CallMatchSource source) {
        this.endpoint = endpoint;
        this.source = source == null ? CallMatchSource.LIVE_QUEUE : source;
        this.enqueuedAt = Instant.now();
    }

    public CallEndpoint endpoint() {
        return endpoint;
    }

    public Instant enqueuedAt() {
        return enqueuedAt;
    }

    public CallMatchSource source() {
        return source;
    }

    public boolean isExpired(Duration timeout) {
        return enqueuedAt.plus(timeout).isBefore(Instant.now());
    }
}
