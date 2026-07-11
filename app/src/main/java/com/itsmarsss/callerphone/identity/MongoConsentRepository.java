package com.itsmarsss.callerphone.identity;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public final class MongoConsentRepository implements ConsentRepository {
    private final MongoCollection<Document> collection;

    public MongoConsentRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.CONSENT_EVENTS);
    }

    @Override
    public void append(ConsentEvent event) {
        collection.insertOne(new Document("_id", event.id())
                .append("userId", event.userId())
                .append("type", event.type().name())
                .append("version", event.version())
                .append("accepted", event.accepted())
                .append("createdAt", BsonTime.toDate(event.createdAt()))
                .append("metadata", event.metadata()));
    }

    @Override
    public List<ConsentEvent> findByUser(String userId, int limit) {
        List<ConsentEvent> events = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("userId", userId))
                .sort(Sorts.descending("createdAt"))
                .limit(limit)) {
            ConsentType type = ConsentType.valueOf(doc.getString("type"));
            events.add(new ConsentEvent(
                    doc.getString("_id"),
                    doc.getString("userId"),
                    type,
                    doc.getString("version"),
                    Boolean.TRUE.equals(doc.getBoolean("accepted")),
                    BsonTime.toInstant(doc.get("createdAt")),
                    doc.getString("metadata")
            ));
        }
        return events;
    }
}
