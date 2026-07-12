package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;

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
        Document doc = new Document("_id", UUID.randomUUID().toString().replace("-", ""))
                .append("userId", userId)
                .append("type", type.name())
                .append("sourceId", sourceId)
                .append("actorDisplay", actorDisplay == null ? "Someone" : actorDisplay)
                .append("preview", preview == null ? "" : preview)
                .append("unread", true)
                .append("priority", priority)
                .append("occurredAt", Instant.now().toString());
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
                Filters.and(Filters.eq("userId", userId), Filters.eq("unread", true)),
                Updates.set("unread", false)
        );
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
