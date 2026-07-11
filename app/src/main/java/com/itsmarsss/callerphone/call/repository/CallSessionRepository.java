package com.itsmarsss.callerphone.call.repository;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.model.CallMatchSource;
import com.itsmarsss.callerphone.call.model.CallMessage;
import com.itsmarsss.callerphone.call.model.CallSession;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.itsmarsss.database.DatabaseUtil.getOrDefault;

/** Persists finished calls into existing `chats` collection (compatible fields). */
public final class CallSessionRepository {
    private static final Logger logger = LoggerFactory.getLogger(CallSessionRepository.class);

    public boolean save(CallSession session) {
        try {
            MongoCollection<Document> col = Callerphone.dbConnector.getChatsCollection();
            Document doc = new Document()
                    .append("id", session.getId())
                    .append("participants", new ArrayList<>(session.getParticipants()))
                    .append("callerChannelId", session.getChannelA())
                    .append("receiverChannelId", session.getChannelB())
                    .append("callerUserId", session.getSideA().starterUserId())
                    .append("receiverUserId", session.getSideB().starterUserId())
                    .append("callerLastMessage", session.getSideALastMessageTime())
                    .append("receiverLastMessage", session.getSideBLastMessageTime())
                    .append("callerMode", "DEFAULT")
                    .append("receiverMode", "DEFAULT")
                    .append("matchSource", session.getSource().name())
                    .append("started", session.getStartedAt().getEpochSecond())
                    .append("ended", session.getEndedAt() != null ? session.getEndedAt().getEpochSecond() : -1L)
                    .append("reported", session.isReported());

            List<Document> msgs = new ArrayList<>();
            for (CallMessage m : session.getMessages()) {
                msgs.add(new Document()
                        .append("fromCaller", m.fromSideA())
                        .append("authorId", m.authorId())
                        .append("authorName", m.authorName())
                        .append("channelId", m.channelId())
                        .append("content", m.content())
                        .append("flags", m.flags())
                        .append("sentAt", m.sentAt().getEpochSecond()));
            }
            doc.append("messages", msgs);
            col.insertOne(doc);
            return true;
        } catch (MongoException e) {
            logger.error("Failed to save call session: {}", e.getMessage());
            return false;
        }
    }

    public Optional<CallSession> findById(String id) {
        try {
            Document doc = Callerphone.dbConnector.getChatsCollection()
                    .find(new Document("id", id)).first();
            if (doc == null) {
                return Optional.empty();
            }
            return Optional.of(fromDocument(doc));
        } catch (Exception e) {
            logger.error("Failed to load call: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void markReported(String id) {
        try {
            Callerphone.dbConnector.getChatsCollection()
                    .updateOne(new Document("id", id), new Document("$set", new Document("reported", true)));
        } catch (Exception e) {
            logger.error("markReported failed: {}", e.getMessage());
        }
    }

    private static CallSession fromDocument(Document doc) {
        String id = getOrDefault(doc, "id", "");
        String a = getOrDefault(doc, "callerChannelId", "");
        String b = getOrDefault(doc, "receiverChannelId", "");
        String userA = getOrDefault(doc, "callerUserId", "");
        String userB = getOrDefault(doc, "receiverUserId", "");
        CallMatchSource source = CallMatchSource.LIVE_QUEUE;
        try {
            source = CallMatchSource.valueOf(getOrDefault(doc, "matchSource", "LIVE_QUEUE"));
        } catch (Exception ignored) {
        }
        CallSession session = new CallSession(
                id,
                CallEndpoint.guild(a, userA),
                CallEndpoint.guild(b, userB),
                source
        );
        if (Boolean.TRUE.equals(doc.getBoolean("reported"))) {
            session.markReported();
        }
        long ended = getOrDefault(doc, "ended", -1L);
        if (ended > 0) {
            // ended already finalized in doc; session object for report only
            session.end();
        }
        List<Document> msgs = doc.getList("messages", Document.class);
        if (msgs != null) {
            for (Document m : msgs) {
                session.addMessage(new CallMessage(
                        getOrDefault(m, "authorId", ""),
                        getOrDefault(m, "authorName", ""),
                        getOrDefault(m, "content", ""),
                        getOrDefault(m, "channelId", ""),
                        Boolean.TRUE.equals(m.getBoolean("fromCaller")),
                        m.getList("flags", String.class) == null ? List.of() : m.getList("flags", String.class),
                        Instant.ofEpochSecond(getOrDefault(m, "sentAt", Instant.now().getEpochSecond()))
                ));
            }
        }
        return session;
    }
}
