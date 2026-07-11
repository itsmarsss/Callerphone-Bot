package com.itsmarsss.callerphone.safety;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class MongoAuditRepository implements AuditRepository {
    private final MongoCollection<Document> collection;

    public MongoAuditRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.AUDIT_EVENTS);
    }

    @Override
    public void append(AuditEvent event) {
        collection.insertOne(new Document("_id", event.id())
                .append("actorId", event.actorId())
                .append("action", event.action())
                .append("targetId", event.targetId())
                .append("product", event.product())
                .append("details", event.details())
                .append("createdAt", BsonTime.toDate(event.createdAt())));
    }
}
