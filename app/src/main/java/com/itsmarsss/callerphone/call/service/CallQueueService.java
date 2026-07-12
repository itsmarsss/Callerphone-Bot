package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.model.CallMatchSource;
import com.itsmarsss.callerphone.call.model.CallQueueEntry;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Live call queues. Guild channels and DMs are matched within their own kind only.
 */
public final class CallQueueService {
    private static final Duration TIMEOUT = Duration.ofMinutes(5);
    private final ConcurrentLinkedQueue<CallQueueEntry> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(CallEndpoint endpoint) {
        remove(endpoint.channelId());
        queue.add(new CallQueueEntry(endpoint, CallMatchSource.LIVE_QUEUE));
    }

    public Optional<CallQueueEntry> dequeueOther(String channelId) {
        return dequeuePeer(CallEndpoint.guild(channelId, null));
    }

    /**
     * Pop the first compatible peer: same endpoint kind, different channel, different user when known.
     */
    public Optional<CallQueueEntry> dequeuePeer(CallEndpoint self) {
        cleanup();
        Iterator<CallQueueEntry> it = queue.iterator();
        while (it.hasNext()) {
            CallQueueEntry entry = it.next();
            CallEndpoint peer = entry.endpoint();
            if (peer.channelId().equals(self.channelId())) {
                continue;
            }
            if (entry.isExpired(TIMEOUT)) {
                it.remove();
                continue;
            }
            if (self.kind() != null && peer.kind() != self.kind()) {
                continue;
            }
            if (self.starterUserId() != null && !self.starterUserId().isBlank()
                    && self.starterUserId().equals(peer.starterUserId())) {
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

    /** Waiting count for a specific kind (guild vs DM). */
    public int size(CallEndpoint.EndpointKind kind) {
        int n = 0;
        for (CallQueueEntry entry : queue) {
            if (!entry.isExpired(TIMEOUT) && entry.endpoint().kind() == kind) {
                n++;
            }
        }
        return n;
    }

    public int position(String channelId, CallEndpoint.EndpointKind kind) {
        int pos = 1;
        for (CallQueueEntry entry : queue) {
            if (entry.isExpired(TIMEOUT) || entry.endpoint().kind() != kind) {
                continue;
            }
            if (entry.endpoint().channelId().equals(channelId)) {
                return pos;
            }
            pos++;
        }
        return -1;
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
