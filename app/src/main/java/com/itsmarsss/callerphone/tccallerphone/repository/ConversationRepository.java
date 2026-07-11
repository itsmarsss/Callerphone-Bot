package com.itsmarsss.callerphone.tccallerphone.repository;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.tccallerphone.entities.ChatMessage;
import com.itsmarsss.callerphone.tccallerphone.entities.ChatMode;
import com.itsmarsss.callerphone.tccallerphone.entities.Conversation;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.itsmarsss.database.DatabaseUtil.getOrDefault;

public class ConversationRepository {
    private static final Logger logger = LoggerFactory.getLogger(ConversationRepository.class);

    public boolean save(Conversation convo) {
        MongoCollection<Document> chatsCollection = Callerphone.dbConnector.getChatsCollection();

        try {
            Document conversationDoc = new Document()
                    .append("id", convo.getId())
                    .append("participants", new ArrayList<String>(convo.getParticipants()))
                    .append("callerChannelId", convo.getCallerChannelId())
                    .append("receiverChannelId", convo.getReceiverChannelId())
                    .append("callerLastMessage", convo.getCallerLastMessageTime())
                    .append("receiverLastMessage", convo.getReceiverLastMessageTime())
                    .append("callerMode", convo.getCallerMode().name())
                    .append("receiverMode", convo.getReceiverMode().name())
                    .append("started", convo.getStartedAt().getEpochSecond())
                    .append("ended", convo.getEndedAt() != null ? convo.getEndedAt().getEpochSecond() : -1L)
                    .append("reported", convo.isReported());

            List<Document> messageDocs = new ArrayList<Document>();
            for (ChatMessage message : convo.getMessages()) {
                messageDocs.add(new Document()
                        .append("fromCaller", message.isFromCaller())
                        .append("authorId", message.getAuthorId())
                        .append("authorName", message.getAuthorName())
                        .append("channelId", message.getChannelId())
                        .append("content", message.getContent())
                        .append("flags", Arrays.asList(message.getFlags()))
                        .append("sentAt", message.getSentAt().getEpochSecond()));
            }
            conversationDoc.append("messages", messageDocs);

            chatsCollection.insertOne(conversationDoc);
            return true;
        } catch (MongoException me) {
            logger.error("Unable to create Chat: {}", me.getMessage());
            return false;
        }
    }

    public Optional<Conversation> findById(String id) {
        MongoCollection<Document> chatsCollection = Callerphone.dbConnector.getChatsCollection();

        try {
            Document doc = chatsCollection.find(new Document("id", id)).first();
            if (doc == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(fromDocument(doc));
        } catch (MongoException me) {
            logger.error("Unable to find Chat: {}", me.getMessage());
            return Optional.empty();
        }
    }

    public boolean markAsReported(String conversationId) {
        MongoCollection<Document> chatsCollection = Callerphone.dbConnector.getChatsCollection();
        try {
            return chatsCollection.updateOne(
                    new Document("id", conversationId),
                    new Document("$set", new Document("reported", true))
            ).getModifiedCount() > 0;
        } catch (MongoException me) {
            logger.error("Unable to mark chat reported: {}", me.getMessage());
            return false;
        }
    }

    private Conversation fromDocument(Document doc) {
        ChatMode callerMode = parseMode(getOrDefault(doc, "callerMode", "DEFAULT"));
        ChatMode receiverMode = parseMode(getOrDefault(doc, "receiverMode", "DEFAULT"));

        // Legacy field fallback
        if (!doc.containsKey("callerMode")) {
            boolean callerAnon = getOrDefault(doc, "callerAnonymous", false);
            boolean receiverAnon = getOrDefault(doc, "receiverAnonymous", false);
            callerMode = callerAnon ? ChatMode.ANONYMOUS : ChatMode.DEFAULT;
            receiverMode = receiverAnon ? ChatMode.ANONYMOUS : ChatMode.DEFAULT;
        }

        String callerId = doc.containsKey("callerChannelId")
                ? getOrDefault(doc, "callerChannelId", "unknown")
                : getOrDefault(doc, "callerTCId", "unknown");
        String receiverId = doc.containsKey("receiverChannelId")
                ? getOrDefault(doc, "receiverChannelId", "unknown")
                : getOrDefault(doc, "receiverTCId", "unknown");

        String id = getOrDefault(doc, "id", ToolSet.generateUID());
        Conversation conversation = new Conversation(id, callerId, receiverId, callerMode, receiverMode);

        // Reconstruct messages for reporting
        List<Document> messageDocs = getOrDefault(doc, "messages", new ArrayList<Document>(), Document.class);
        for (Document messageDoc : messageDocs) {
            boolean fromCaller = messageDoc.containsKey("fromCaller")
                    ? getOrDefault(messageDoc, "fromCaller", false)
                    : getOrDefault(messageDoc, "caller", false);
            String authorId = messageDoc.containsKey("authorId")
                    ? getOrDefault(messageDoc, "authorId", "unknown")
                    : getOrDefault(messageDoc, "author", "unknown");
            String channelId = messageDoc.containsKey("channelId")
                    ? getOrDefault(messageDoc, "channelId", "unknown")
                    : getOrDefault(messageDoc, "channel", "unknown");
            long sent = messageDoc.containsKey("sentAt")
                    ? getOrDefault(messageDoc, "sentAt", -1L)
                    : getOrDefault(messageDoc, "sent", -1L);

            List<String> flagList = getOrDefault(messageDoc, "flags", new ArrayList<String>(), String.class);
            ChatMessage message = new ChatMessage(
                    authorId,
                    getOrDefault(messageDoc, "authorName", "Unknown"),
                    getOrDefault(messageDoc, "content", ""),
                    channelId,
                    fromCaller,
                    flagList.toArray(new String[0]),
                    sent > 0 ? Instant.ofEpochSecond(sent) : Instant.EPOCH
            );
            conversation.addMessage(message);
        }

        if (getOrDefault(doc, "reported", false) || getOrDefault(doc, "report", false)) {
            conversation.markAsReported();
        }
        conversation.end();

        return conversation;
    }

    private static ChatMode parseMode(String name) {
        try {
            return ChatMode.valueOf(name);
        } catch (Exception e) {
            return ChatMode.DEFAULT;
        }
    }
}
