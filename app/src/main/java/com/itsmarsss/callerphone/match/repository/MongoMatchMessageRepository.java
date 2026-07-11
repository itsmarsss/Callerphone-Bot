package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.MatchMessage;
import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public final class MongoMatchMessageRepository implements MatchMessageRepository {
    private final MongoCollection<Document> collection;

    public MongoMatchMessageRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.MATCH_MESSAGES);
    }

    @Override
    public void save(MatchMessage message) {
        collection.replaceOne(
                Filters.eq("_id", message.messageId()),
                new Document("_id", message.messageId())
                        .append("conversationId", message.conversationId())
                        .append("senderId", message.senderId())
                        .append("recipientId", message.recipientId())
                        .append("content", message.content())
                        .append("createdAt", BsonTime.toDate(message.createdAt()))
                        .append("deliveryStatus", message.deliveryStatus()),
                new com.mongodb.client.model.ReplaceOptions().upsert(true)
        );
    }

    @Override
    public List<MatchMessage> findByConversation(String conversationId, int limit) {
        List<MatchMessage> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("conversationId", conversationId))
                .sort(Sorts.descending("createdAt"))
                .limit(limit)) {
            results.add(new MatchMessage(
                    doc.getString("_id"),
                    doc.getString("conversationId"),
                    doc.getString("senderId"),
                    doc.getString("recipientId"),
                    doc.getString("content"),
                    BsonTime.toInstant(doc.get("createdAt")),
                    doc.getString("deliveryStatus")
            ));
        }
        return results;
    }
}
