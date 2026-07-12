package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Plan philosophy §10.F — unified social inbox (Mongo-backed).
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

    private final MongoCollection<Document> collection;

    public SocialInboxService(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.SOCIAL_INBOX);
    }

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
        String actor = actorDisplay == null ? "Someone" : actorDisplay;
        String text = preview == null ? "" : preview;
        Instant now = Instant.now();

        // Coalesce chat/bottle bursts: one unread row per source, refresh preview
        if ((type == EntryType.CONNECTION_MESSAGE || type == EntryType.BOTTLE_REPLY)
                && sourceId != null && !sourceId.isBlank()) {
            Bson filter = Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("type", type.name()),
                    Filters.eq("sourceId", sourceId),
                    Filters.eq("unread", true)
            );
            Document existing = collection.find(filter).first();
            if (existing != null) {
                collection.updateOne(filter, Updates.combine(
                        Updates.set("actorDisplay", actor),
                        Updates.set("preview", text),
                        Updates.set("occurredAt", now.toString()),
                        Updates.set("priority", priority)
                ));
                return;
            }
        }

        Document doc = new Document("_id", UUID.randomUUID().toString().replace("-", ""))
                .append("userId", userId)
                .append("type", type.name())
                .append("sourceId", sourceId)
                .append("actorDisplay", actor)
                .append("preview", text)
                .append("unread", true)
                .append("priority", priority)
                .append("occurredAt", now.toString());
        collection.insertOne(doc);
    }

    public List<InboxEntry> list(String userId, int limit) {
        List<InboxEntry> out = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("userId", userId))
                .sort(Sorts.orderBy(Sorts.ascending("priority"), Sorts.descending("occurredAt")))
                .limit(Math.max(1, limit))) {
            out.add(fromDoc(doc));
        }
        return out;
    }

    public int unreadCount(String userId) {
        return (int) collection.countDocuments(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("unread", true)
        ));
    }

    public void markRead(String userId, String entryId) {
        collection.updateOne(
                Filters.and(Filters.eq("_id", entryId), Filters.eq("userId", userId)),
                Updates.set("unread", false)
        );
    }

    public void markAllRead(String userId) {
        collection.updateMany(
                Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("unread", true),
                        Filters.ne("type", EntryType.SAFETY_UPDATE.name())
                ),
                Updates.set("unread", false)
        );
    }

    public OptionalInboxEntry find(String userId, String entryId) {
        if (userId == null || entryId == null) {
            return OptionalInboxEntry.empty();
        }
        Document doc = collection.find(Filters.and(
                Filters.eq("_id", entryId),
                Filters.eq("userId", userId)
        )).first();
        if (doc == null) {
            return OptionalInboxEntry.empty();
        }
        return OptionalInboxEntry.of(fromDoc(doc));
    }

    public OptionalInboxEntry firstUnread(String userId) {
        Document doc = collection.find(Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("unread", true)
                ))
                .sort(Sorts.orderBy(Sorts.ascending("priority"), Sorts.descending("occurredAt")))
                .first();
        if (doc == null) {
            return OptionalInboxEntry.empty();
        }
        return OptionalInboxEntry.of(fromDoc(doc));
    }

    public void markReadBySource(String userId, String sourceId) {
        if (userId == null || sourceId == null || sourceId.isBlank()) {
            return;
        }
        collection.updateMany(
                Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("sourceId", sourceId),
                        Filters.eq("unread", true)
                ),
                Updates.set("unread", false)
        );
    }

    /** Lightweight optional without java.util.Optional for simple call sites. */
    public record OptionalInboxEntry(InboxEntry entry) {
        public static OptionalInboxEntry empty() {
            return new OptionalInboxEntry(null);
        }

        public static OptionalInboxEntry of(InboxEntry entry) {
            return new OptionalInboxEntry(entry);
        }

        public boolean isPresent() {
            return entry != null;
        }

        public InboxEntry get() {
            return entry;
        }
    }

    private static InboxEntry fromDoc(Document doc) {
        EntryType type;
        try {
            type = EntryType.valueOf(doc.getString("type"));
        } catch (Exception e) {
            type = EntryType.CONNECTION_MESSAGE;
        }
        Instant at;
        try {
            at = Instant.parse(doc.getString("occurredAt"));
        } catch (Exception e) {
            at = Instant.now();
        }
        return new InboxEntry(
                doc.getString("_id"),
                doc.getString("userId"),
                type,
                doc.getString("sourceId"),
                doc.getString("actorDisplay"),
                doc.getString("preview"),
                Boolean.TRUE.equals(doc.getBoolean("unread")),
                doc.getInteger("priority", 9),
                at
        );
    }
}
