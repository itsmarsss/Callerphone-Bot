package com.itsmarsss.callerphone.safety;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public final class MongoBlockRepository implements BlockRepository {
    private final MongoCollection<Document> collection;

    public MongoBlockRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.BLOCKS);
    }

    @Override
    public boolean existsEitherDirection(String firstUserId, String secondUserId, String product) {
        return collection.find(Filters.or(
                Filters.and(
                        Filters.eq("blockerId", firstUserId),
                        Filters.eq("blockedId", secondUserId),
                        Filters.in("product", product, Block.PRODUCT_GLOBAL)
                ),
                Filters.and(
                        Filters.eq("blockerId", secondUserId),
                        Filters.eq("blockedId", firstUserId),
                        Filters.in("product", product, Block.PRODUCT_GLOBAL)
                )
        )).first() != null;
    }

    @Override
    public void create(Block block) {
        try {
            collection.insertOne(new Document("blockerId", block.blockerId())
                    .append("blockedId", block.blockedId())
                    .append("product", block.product())
                    .append("createdAt", BsonTime.toDate(block.createdAt()))
                    .append("reason", block.reason()));
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() != ErrorCategory.DUPLICATE_KEY) {
                throw e;
            }
        }
    }
}
