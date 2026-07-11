package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.model.CallMatchSource;
import com.itsmarsss.callerphone.call.model.CallQueueEntry;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Single live queue — no mode partitioning (anon/FF removed). */
public final class CallQueueService {
    private static final Duration TIMEOUT = Duration.ofMinutes(5);
    private final ConcurrentLinkedQueue<CallQueueEntry> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(CallEndpoint endpoint) {
        remove(endpoint.channelId());
        queue.add(new CallQueueEntry(endpoint, CallMatchSource.LIVE_QUEUE));
    }

    public Optional<CallQueueEntry> dequeueOther(String channelId) {
        cleanup();
        Iterator<CallQueueEntry> it = queue.iterator();
        while (it.hasNext()) {
            CallQueueEntry entry = it.next();
            if (entry.endpoint().channelId().equals(channelId)) {
                continue;
            }
            if (entry.isExpired(TIMEOUT)) {
                it.remove();
                continue;
            }
            it.remove();
            return Optional.of(entry);
        }
        return Optional.empty();
    }

    public boolean remove(String channelId) {
        boolean removed = false;
        Iterator<CallQueueEntry> it = queue.iterator();
        while (it.hasNext()) {
            if (it.next().endpoint().channelId().equals(channelId)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    public boolean isQueued(String channelId) {
        for (CallQueueEntry entry : queue) {
            if (entry.endpoint().channelId().equals(channelId) && !entry.isExpired(TIMEOUT)) {
                return true;
            }
        }
        return false;
    }

    public int position(String channelId) {
        int pos = 1;
        for (CallQueueEntry entry : queue) {
            if (entry.isExpired(TIMEOUT)) {
                continue;
            }
            if (entry.endpoint().channelId().equals(channelId)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    public int size() {
        int n = 0;
        for (CallQueueEntry entry : queue) {
            if (!entry.isExpired(TIMEOUT)) {
                n++;
            }
        }
        return n;
    }

    public void cleanup() {
        Iterator<CallQueueEntry> it = queue.iterator();
        while (it.hasNext()) {
            if (it.next().isExpired(TIMEOUT)) {
                it.remove();
            }
        }
    }
}
