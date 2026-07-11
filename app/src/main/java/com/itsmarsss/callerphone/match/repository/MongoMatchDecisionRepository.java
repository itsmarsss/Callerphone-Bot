package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.DecisionType;
import com.itsmarsss.callerphone.match.model.MatchDecision;
import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class MongoMatchDecisionRepository implements MatchDecisionRepository {
    private final MongoCollection<Document> collection;

    public MongoMatchDecisionRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.MATCH_DECISIONS);
    }

    @Override
    public void upsert(MatchDecision decision) {
        Document doc = new Document("viewerId", decision.viewerId())
                .append("subjectId", decision.subjectId())
                .append("decision", decision.decision().name())
                .append("createdAt", BsonTime.toDate(decision.createdAt()))
                .append("expiresAt", BsonTime.toDate(decision.expiresAt()));
        collection.replaceOne(
                Filters.and(
                        Filters.eq("viewerId", decision.viewerId()),
                        Filters.eq("subjectId", decision.subjectId())
                ),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public Optional<MatchDecision> find(String viewerId, String subjectId) {
        Document doc = collection.find(Filters.and(
                Filters.eq("viewerId", viewerId),
                Filters.eq("subjectId", subjectId)
        )).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public boolean hasActiveDecision(String viewerId, String subjectId) {
        return find(viewerId, subjectId)
                .filter(d -> !d.isExpired(Instant.now()))
                .isPresent();
    }

    @Override
    public Set<String> findSubjectIdsForViewer(String viewerId) {
        Set<String> ids = new HashSet<>();
        Instant now = Instant.now();
        for (Document doc : collection.find(Filters.eq("viewerId", viewerId))) {
            MatchDecision decision = fromDocument(doc);
            if (!decision.isExpired(now)) {
                ids.add(decision.subjectId());
            }
        }
        return ids;
    }

    @Override
    public List<MatchDecision> findIncomingInterested(String subjectId, int limit) {
        List<MatchDecision> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.and(
                        Filters.eq("subjectId", subjectId),
                        Filters.eq("decision", DecisionType.INTERESTED.name())))
                .sort(Sorts.descending("createdAt"))
                .limit(limit)) {
            results.add(fromDocument(doc));
        }
        return results;
    }

    @Override
    public Optional<MatchDecision> findReciprocalInterest(String firstUserId, String secondUserId) {
        return find(firstUserId, secondUserId)
                .filter(d -> d.decision() == DecisionType.INTERESTED && !d.isExpired(Instant.now()));
    }

    @Override
    public void delete(String viewerId, String subjectId) {
        collection.deleteOne(Filters.and(
                Filters.eq("viewerId", viewerId),
                Filters.eq("subjectId", subjectId)
        ));
    }

    private static MatchDecision fromDocument(Document doc) {
        return new MatchDecision(
                doc.getString("viewerId"),
                doc.getString("subjectId"),
                DecisionType.from(doc.getString("decision")).orElse(DecisionType.SKIP),
                BsonTime.toInstant(doc.get("createdAt")),
                BsonTime.toInstant(doc.get("expiresAt"))
        );
    }
}
