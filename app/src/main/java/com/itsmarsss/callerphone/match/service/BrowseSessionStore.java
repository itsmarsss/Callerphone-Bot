package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class BrowseSessionStore {
    private final MongoCollection<Document> collection;

    public BrowseSessionStore(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.BROWSE_SESSIONS);
    }

    public BrowseSession create(String viewerId, String subjectId) {
        BrowseSession session = new BrowseSession(
                UUID.randomUUID().toString(),
                viewerId,
                subjectId,
                Instant.now().plus(Duration.ofMinutes(15))
        );
        collection.replaceOne(
                Filters.eq("_id", session.sessionId()),
                new Document("_id", session.sessionId())
                        .append("viewerId", session.viewerId())
                        .append("subjectId", session.subjectId())
                        .append("expiresAt", BsonTime.toDate(session.expiresAt())),
                new ReplaceOptions().upsert(true)
        );
        return session;
    }

    public Optional<BrowseSession> find(String sessionId) {
        Document doc = collection.find(Filters.eq("_id", sessionId)).first();
        if (doc == null) {
            return Optional.empty();
        }
        return Optional.of(new BrowseSession(
                doc.getString("_id"),
                doc.getString("viewerId"),
                doc.getString("subjectId"),
                BsonTime.toInstant(doc.get("expiresAt"))
        ));
    }

    public void delete(String sessionId) {
        collection.deleteOne(Filters.eq("_id", sessionId));
    }
}
