package com.itsmarsss.callerphone.analytics;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class MongoProductEventRepository implements ProductEventRepository {
    private final MongoCollection<Document> collection;

    public MongoProductEventRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.PRODUCT_EVENTS);
    }

    @Override
    public void append(ProductEvent event) {
        collection.insertOne(new Document("_id", event.id())
                .append("userId", event.userId())
                .append("name", event.name())
                .append("metadata", event.metadata())
                .append("createdAt", BsonTime.toDate(event.createdAt())));
    }
}
