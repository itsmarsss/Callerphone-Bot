package com.itsmarsss.callerphone.match.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Plan philosophy §10.F — unified social inbox (MVP in-memory).
 * Survives only process lifetime; Mongo persistence can replace later.
 */
public final class SocialInboxService {
    public enum EntryType {
        CONNECTION_MESSAGE,
        BOTTLE_REPLY,
        GAME_INVITE,
        GAME_TURN,
        PROFILE_SHARE_RESPONSE,
        SAFETY_UPDATE,
        INCOMING_INTEREST
    }

    public record InboxEntry(
            String id,
            String userId,
            EntryType type,
            String sourceId,
            String actorDisplay,
            String preview,
            boolean unread,
            int priority,
            Instant occurredAt
    ) {
    }

    private final Map<String, CopyOnWriteArrayList<InboxEntry>> byUser = new ConcurrentHashMap<>();

    public void push(
            String userId,
            EntryType type,
            String sourceId,
            String actorDisplay,
            String preview
    ) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        int priority = switch (type) {
            case SAFETY_UPDATE -> 1;
            case CONNECTION_MESSAGE -> 2;
            case BOTTLE_REPLY -> 3;
            case GAME_INVITE -> 4;
            case GAME_TURN -> 5;
            case PROFILE_SHARE_RESPONSE -> 6;
            case INCOMING_INTEREST -> 7;
        };
        InboxEntry entry = new InboxEntry(
                java.util.UUID.randomUUID().toString().replace("-", ""),
                userId,
                type,
                sourceId,
                actorDisplay == null ? "Someone" : actorDisplay,
                preview == null ? "" : preview,
                true,
                priority,
                Instant.now()
        );
        byUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(0, entry);
        // Cap per user
        CopyOnWriteArrayList<InboxEntry> list = byUser.get(userId);
        while (list.size() > 50) {
            list.remove(list.size() - 1);
        }
    }

    public List<InboxEntry> list(String userId, int limit) {
        List<InboxEntry> all = byUser.getOrDefault(userId, new CopyOnWriteArrayList<>());
        List<InboxEntry> copy = new ArrayList<>(all);
        copy.sort(Comparator
                .comparingInt(InboxEntry::priority)
                .thenComparing(InboxEntry::occurredAt, Comparator.reverseOrder()));
        if (copy.size() <= limit) {
            return copy;
        }
        return copy.subList(0, limit);
    }

    public int unreadCount(String userId) {
        return (int) byUser.getOrDefault(userId, new CopyOnWriteArrayList<>()).stream()
                .filter(InboxEntry::unread)
                .count();
    }

    public void markRead(String userId, String entryId) {
        CopyOnWriteArrayList<InboxEntry> list = byUser.get(userId);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            InboxEntry e = list.get(i);
            if (e.id().equals(entryId) && e.unread()) {
                list.set(i, new InboxEntry(
                        e.id(), e.userId(), e.type(), e.sourceId(), e.actorDisplay(),
                        e.preview(), false, e.priority(), e.occurredAt()
                ));
            }
        }
    }
}
