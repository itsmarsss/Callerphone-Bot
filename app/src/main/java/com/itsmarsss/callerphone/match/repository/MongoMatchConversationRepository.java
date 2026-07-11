package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MongoMatchConversationRepository implements MatchConversationRepository {
    private final MongoCollection<Document> collection;

    public MongoMatchConversationRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
    }

    @Override
    public Optional<MatchConversation> findById(String conversationId) {
        Document doc = collection.find(Filters.eq("_id", conversationId)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public Optional<MatchConversation> findByMatchId(String matchId) {
        Document doc = collection.find(Filters.eq("matchId", matchId)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public void save(MatchConversation conversation) {
        collection.replaceOne(
                Filters.eq("_id", conversation.getConversationId()),
                toDocument(conversation),
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public List<MatchConversation> findActiveByUserId(String userId) {
        List<MatchConversation> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.and(
                        Filters.eq("participants", userId),
                        Filters.ne("stage", ConversationStage.ARCHIVED.name())))
                .sort(Sorts.descending("lastActivityAt"))) {
            results.add(fromDocument(doc));
        }
        return results;
    }

    @Override
    public long countActiveByUserId(String userId) {
        return collection.countDocuments(Filters.and(
                Filters.eq("participants", userId),
                Filters.ne("stage", ConversationStage.ARCHIVED.name())
        ));
    }

    private static Document toDocument(MatchConversation c) {
        Document unread = new Document();
        if (c.getUnreadByUser() != null) {
            c.getUnreadByUser().forEach(unread::append);
        }
        return new Document("_id", c.getConversationId())
                .append("matchId", c.getMatchId())
                .append("participants", c.getParticipants())
                .append("stage", c.getStage().name())
                .append("connectRequestedBy", c.getConnectRequestedBy())
                .append("connectRequestedAt", BsonTime.toDate(c.getConnectRequestedAt()))
                .append("connectExpiresAt", BsonTime.toDate(c.getConnectExpiresAt()))
                .append("messageCount", c.getMessageCount())
                .append("createdAt", BsonTime.toDate(c.getCreatedAt()))
                .append("lastActivityAt", BsonTime.toDate(c.getLastActivityAt()))
                .append("connectedAt", BsonTime.toDate(c.getConnectedAt()))
                .append("archivedAt", BsonTime.toDate(c.getArchivedAt()))
                .append("unreadByUser", unread)
                .append("lastMessagePreview", c.getLastMessagePreview())
                .append("lastNudgeAt", BsonTime.toDate(c.getLastNudgeAt()))
                .append("chatStreakDays", c.getChatStreakDays())
                .append("lastChatDay", c.getLastChatDay());
    }

    private static MatchConversation fromDocument(Document doc) {
        MatchConversation c = new MatchConversation();
        c.setConversationId(doc.getString("_id"));
        c.setMatchId(doc.getString("matchId"));
        c.setParticipants(doc.getList("participants", String.class));
        c.setStage(ConversationStage.from(doc.getString("stage")).orElse(ConversationStage.MEDIATED));
        c.setConnectRequestedBy(doc.getString("connectRequestedBy"));
        c.setConnectRequestedAt(BsonTime.toInstant(doc.get("connectRequestedAt")));
        c.setConnectExpiresAt(BsonTime.toInstant(doc.get("connectExpiresAt")));
        Number count = (Number) doc.get("messageCount");
        c.setMessageCount(count == null ? 0 : count.longValue());
        c.setCreatedAt(BsonTime.toInstant(doc.get("createdAt")));
        c.setLastActivityAt(BsonTime.toInstant(doc.get("lastActivityAt")));
        c.setConnectedAt(BsonTime.toInstant(doc.get("connectedAt")));
        c.setArchivedAt(BsonTime.toInstant(doc.get("archivedAt")));
        Document unread = doc.get("unreadByUser", Document.class);
        if (unread != null) {
            java.util.Map<String, Integer> map = new java.util.HashMap<>();
            for (String key : unread.keySet()) {
                Object v = unread.get(key);
                if (v instanceof Number n) {
                    map.put(key, n.intValue());
                }
            }
            c.setUnreadByUser(map);
        }
        c.setLastMessagePreview(doc.getString("lastMessagePreview"));
        c.setLastNudgeAt(BsonTime.toInstant(doc.get("lastNudgeAt")));
        c.setChatStreakDays(doc.getInteger("chatStreakDays", 0));
        c.setLastChatDay(doc.getString("lastChatDay"));
        return c;
    }
}
