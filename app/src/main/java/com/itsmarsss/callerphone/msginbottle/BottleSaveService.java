package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persisted "Save" bookmarks for bottles (plan §27). */
public final class BottleSaveService {
    private final MongoCollection<Document> collection;

    public BottleSaveService(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.BOTTLE_SAVES);
    }

    public void save(String userId, String bottleId) {
        if (userId == null || userId.isBlank() || bottleId == null || bottleId.isBlank()) {
            return;
        }
        collection.updateOne(
                Filters.and(Filters.eq("userId", userId), Filters.eq("bottleId", bottleId)),
                Updates.combine(
                        Updates.setOnInsert("userId", userId),
                        Updates.setOnInsert("bottleId", bottleId),
                        Updates.set("savedAt", Instant.now().toString())
                ),
                new UpdateOptions().upsert(true)
        );
    }

    public List<String> listBottleIds(String userId, int limit) {
        List<String> out = new ArrayList<>();
        if (userId == null || userId.isBlank()) {
            return out;
        }
        for (Document doc : collection.find(Filters.eq("userId", userId))
                .sort(Sorts.descending("savedAt"))
                .limit(Math.max(1, Math.min(limit, 50)))) {
            String id = doc.getString("bottleId");
            if (id != null && !id.isBlank()) {
                out.add(id);
            }
        }
        return out;
    }
}
