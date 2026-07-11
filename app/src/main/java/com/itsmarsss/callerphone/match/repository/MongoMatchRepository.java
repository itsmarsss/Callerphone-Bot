package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.Match;
import com.itsmarsss.callerphone.match.model.MatchStatus;
import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MongoMatchRepository implements MatchRepository {
    private final MongoCollection<Document> collection;

    public MongoMatchRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.MATCHES);
    }

    @Override
    public Optional<Match> findById(String matchId) {
        Document doc = collection.find(Filters.eq("_id", matchId)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public Optional<Match> findByPairKey(String pairKey) {
        Document doc = collection.find(Filters.eq("pairKey", pairKey)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public Optional<Match> createIfAbsent(Match match) {
        if (match.getMatchId() == null || match.getMatchId().isBlank()) {
            match.setMatchId(UUID.randomUUID().toString());
        }
        try {
            collection.insertOne(toDocument(match));
            return Optional.of(match);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                return findByPairKey(match.getPairKey());
            }
            throw e;
        }
    }

    @Override
    public void save(Match match) {
        collection.replaceOne(
                Filters.eq("_id", match.getMatchId()),
                toDocument(match),
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public List<Match> findActiveByUserId(String userId) {
        List<Match> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.and(
                        Filters.eq("userIds", userId),
                        Filters.eq("status", MatchStatus.ACTIVE.name())))
                .sort(Sorts.descending("matchedAt"))) {
            results.add(fromDocument(doc));
        }
        return results;
    }

    private static Document toDocument(Match match) {
        return new Document("_id", match.getMatchId())
                .append("pairKey", match.getPairKey())
                .append("userIds", match.getUserIds())
                .append("status", match.getStatus().name())
                .append("matchedAt", BsonTime.toDate(match.getMatchedAt()))
                .append("endedAt", BsonTime.toDate(match.getEndedAt()))
                .append("endedBy", match.getEndedBy());
    }

    private static Match fromDocument(Document doc) {
        Match match = new Match();
        match.setMatchId(doc.getString("_id"));
        match.setPairKey(doc.getString("pairKey"));
        match.setUserIds(doc.getList("userIds", String.class));
        match.setStatus(MatchStatus.from(doc.getString("status")).orElse(MatchStatus.ACTIVE));
        match.setMatchedAt(BsonTime.toInstant(doc.get("matchedAt")));
        match.setEndedAt(BsonTime.toInstant(doc.get("endedAt")));
        match.setEndedBy(doc.getString("endedBy"));
        return match;
    }
}
