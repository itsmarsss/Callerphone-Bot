package com.itsmarsss.callerphone.identity;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MongoMatchUserRepository implements MatchUserRepository {
    private final MongoCollection<Document> collection;

    public MongoMatchUserRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.MATCH_USERS);
    }

    @Override
    public Optional<MatchUser> findById(String userId) {
        Document doc = collection.find(Filters.eq("_id", userId)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public void save(MatchUser user) {
        collection.replaceOne(
                Filters.eq("_id", user.getUserId()),
                toDocument(user),
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public List<MatchUser> findDigestOptIn(int limit) {
        List<MatchUser> out = new ArrayList<>();
        for (Document doc : collection.find(Filters.and(
                        Filters.eq("enrolled", true),
                        Filters.eq("digestOptIn", true),
                        Filters.eq("notificationsEnabled", true)))
                .limit(limit)) {
            out.add(fromDocument(doc));
        }
        return out;
    }

    private static Document toDocument(MatchUser user) {
        return new Document("_id", user.getUserId())
                .append("schemaVersion", user.getSchemaVersion())
                .append("enrolled", user.isEnrolled())
                .append("ageCohort", user.getAgeCohort() == null ? null : user.getAgeCohort().code())
                .append("ageSelectedAt", BsonTime.toDate(user.getAgeSelectedAt()))
                .append("notificationsEnabled", user.isNotificationsEnabled())
                .append("createdAt", BsonTime.toDate(user.getCreatedAt()))
                .append("lastActiveAt", BsonTime.toDate(user.getLastActiveAt()))
                .append("termsVersionAccepted", user.getTermsVersionAccepted())
                .append("privacyVersionAccepted", user.getPrivacyVersionAccepted())
                .append("enrolledAt", BsonTime.toDate(user.getEnrolledAt()))
                .append("leftAt", BsonTime.toDate(user.getLeftAt()))
                .append("selectedConversationId", user.getSelectedConversationId())
                .append("conversationSelectedAt", BsonTime.toDate(user.getConversationSelectedAt()))
                .append("lastSkipSubjectId", user.getLastSkipSubjectId())
                .append("lastSkipAt", BsonTime.toDate(user.getLastSkipAt()))
                .append("undosToday", user.getUndosToday())
                .append("usageDay", user.getUsageDay())
                .append("digestOptIn", user.isDigestOptIn())
                .append("lastDigestAt", BsonTime.toDate(user.getLastDigestAt()))
                .append("browseStreakDays", user.getBrowseStreakDays())
                .append("lastBrowseDay", user.getLastBrowseDay());
    }

    private static MatchUser fromDocument(Document doc) {
        MatchUser user = new MatchUser(doc.getString("_id"));
        user.setSchemaVersion(doc.getInteger("schemaVersion", 1));
        user.setEnrolled(Boolean.TRUE.equals(doc.getBoolean("enrolled")));
        user.setAgeCohort(AgeCohort.fromCode(doc.getString("ageCohort")).orElse(null));
        user.setAgeSelectedAt(BsonTime.toInstant(doc.get("ageSelectedAt")));
        user.setNotificationsEnabled(doc.getBoolean("notificationsEnabled", true));
        user.setCreatedAt(BsonTime.toInstant(doc.get("createdAt")));
        user.setLastActiveAt(BsonTime.toInstant(doc.get("lastActiveAt")));
        user.setTermsVersionAccepted(doc.getString("termsVersionAccepted"));
        user.setPrivacyVersionAccepted(doc.getString("privacyVersionAccepted"));
        user.setEnrolledAt(BsonTime.toInstant(doc.get("enrolledAt")));
        user.setLeftAt(BsonTime.toInstant(doc.get("leftAt")));
        user.setSelectedConversationId(doc.getString("selectedConversationId"));
        user.setConversationSelectedAt(BsonTime.toInstant(doc.get("conversationSelectedAt")));
        user.setLastSkipSubjectId(doc.getString("lastSkipSubjectId"));
        user.setLastSkipAt(BsonTime.toInstant(doc.get("lastSkipAt")));
        user.setUndosToday(doc.getInteger("undosToday", 0));
        user.setUsageDay(doc.getString("usageDay"));
        user.setDigestOptIn(Boolean.TRUE.equals(doc.getBoolean("digestOptIn")));
        user.setLastDigestAt(BsonTime.toInstant(doc.get("lastDigestAt")));
        user.setBrowseStreakDays(doc.getInteger("browseStreakDays", 0));
        user.setLastBrowseDay(doc.getString("lastBrowseDay"));
        return user;
    }
}
