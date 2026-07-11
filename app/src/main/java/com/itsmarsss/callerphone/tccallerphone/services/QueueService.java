package com.itsmarsss.callerphone.tccallerphone.services;

import com.itsmarsss.callerphone.tccallerphone.entities.ChatMode;
import com.itsmarsss.callerphone.tccallerphone.entities.QueueEntry;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueService {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    private final ConcurrentLinkedQueue<QueueEntry> queue = new ConcurrentLinkedQueue<QueueEntry>();
    private final Duration queueTimeout;

    public QueueService() {
        this(DEFAULT_TIMEOUT);
    }

    public QueueService(Duration queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    public void enqueue(String channelId, ChatMode mode) {
        removeFromQueue(channelId);
        queue.add(new QueueEntry(channelId, mode));
    }

    public Optional<QueueEntry> dequeueMatch(String channelId) {
        cleanupExpiredEntries();

        Iterator<QueueEntry> iterator = queue.iterator();
        while (iterator.hasNext()) {
            QueueEntry entry = iterator.next();
            if (entry.getChannelId().equals(channelId)) {
                continue;
            }
            if (entry.isExpired(queueTimeout)) {
                iterator.remove();
                continue;
            }
            iterator.remove();
            return Optional.of(entry);
        }
        return Optional.empty();
    }

    public boolean removeFromQueue(String channelId) {
        boolean removed = false;
        Iterator<QueueEntry> iterator = queue.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getChannelId().equals(channelId)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    public boolean isQueued(String channelId) {
        for (QueueEntry entry : queue) {
            if (entry.getChannelId().equals(channelId)) {
                return true;
            }
        }
        return false;
    }

    public int getQueuePosition(String channelId) {
        int position = 1;
        for (QueueEntry entry : queue) {
            if (entry.getChannelId().equals(channelId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public int size() {
        return queue.size();
    }

    public void cleanupExpiredEntries() {
        Iterator<QueueEntry> iterator = queue.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired(queueTimeout)) {
                iterator.remove();
            }
        }
    }
}
