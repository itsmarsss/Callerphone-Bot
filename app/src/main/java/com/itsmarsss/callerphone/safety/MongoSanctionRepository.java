package com.itsmarsss.callerphone.safety;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class MongoSanctionRepository implements SanctionRepository {
    private final MongoCollection<Document> collection;

    public MongoSanctionRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.SANCTIONS);
    }

    @Override
    public void save(Sanction sanction) {
        collection.replaceOne(
                Filters.eq("_id", sanction.getId()),
                toDocument(sanction),
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public boolean hasActiveSanction(String userId, String product) {
        Instant now = Instant.now();
        for (Sanction sanction : findActive(userId)) {
            if (!sanction.isEffective(now)) {
                continue;
            }
            if (Block.PRODUCT_GLOBAL.equals(sanction.getProduct())
                    || product.equals(sanction.getProduct())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Sanction> findActive(String userId) {
        List<Sanction> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("active", true)))) {
            results.add(fromDocument(doc));
        }
        return results;
    }

    private static Document toDocument(Sanction s) {
        return new Document("_id", s.getId())
                .append("userId", s.getUserId())
                .append("product", s.getProduct())
                .append("type", s.getType())
                .append("active", s.isActive())
                .append("reason", s.getReason())
                .append("moderatorId", s.getModeratorId())
                .append("createdAt", BsonTime.toDate(s.getCreatedAt()))
                .append("expiresAt", BsonTime.toDate(s.getExpiresAt()));
    }

    private static Sanction fromDocument(Document doc) {
        Sanction s = new Sanction();
        s.setId(doc.getString("_id"));
        s.setUserId(doc.getString("userId"));
        s.setProduct(doc.getString("product"));
        s.setType(doc.getString("type"));
        s.setActive(Boolean.TRUE.equals(doc.getBoolean("active")));
        s.setReason(doc.getString("reason"));
        s.setModeratorId(doc.getString("moderatorId"));
        s.setCreatedAt(BsonTime.toInstant(doc.get("createdAt")));
        s.setExpiresAt(BsonTime.toInstant(doc.get("expiresAt")));
        return s;
    }
}
