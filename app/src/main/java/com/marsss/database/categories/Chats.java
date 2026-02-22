package com.marsss.database.categories;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.entities.TCConversation;
import com.marsss.callerphone.tccallerphone.entities.TCMessage;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.marsss.database.DatabaseUtil.getOrDefault;

public class Chats {
    public static final Logger logger = LoggerFactory.getLogger(Chats.class);

    public static boolean createChat(TCConversation convo) {
        MongoCollection<Document> chatsCollection = Callerphone.dbConnector.getChatsCollection();

        try {
            Document conversationDoc = new Document()
                    .append("id", convo.getId())
                    .append("participants", convo.getParticipants())
                    .append("callerTCId", convo.getCallerTCId())
                    .append("receiverTCId", convo.getReceiverTCId())
                    .append("callerLastMessage", convo.getCallerLastMessage())
                    .append("receiverLastMessage", convo.getReceiverLastMessage())
                    .append("callerAnonymous", convo.getCallerAnonymous())
                    .append("receiverAnonymous", convo.getReceiverAnonymous())
                    .append("started", convo.getStarted())
                    .append("ended", convo.getEnded())
                    .append("report", convo.getReport());

            List<Document> messageDocs = new ArrayList<>();
            for (TCMessage message : convo.getMessages()) {
                Document messageDoc = new Document()
                        .append("caller", message.isCaller())
                        .append("author", message.getAuthor())
                        .append("channel", message.getChannel())
                        .append("content", message.getContent())
                        .append("flags", Arrays.asList(message.getFlags()))
                        .append("sent", message.getSent());
                messageDocs.add(messageDoc);
            }

            conversationDoc.append("messages", messageDocs);

            chatsCollection.insertOne(conversationDoc);
            return true;
        } catch (MongoException me) {
            logger.error("Unable to create Chat: {}", me.getMessage());
        }
        return false;
    }

    public static TCConversation queryChat(String id) {
        MongoCollection<Document> chatsCollection = Callerphone.dbConnector.getChatsCollection();

        try {
            Document conversationDocument = chatsCollection.find(new Document("id", id)).first();

            TCConversation conversation = new TCConversation();
            conversation.setId(getOrDefault(conversationDocument, "id", "unknown"));
            conversation.setParticipants(getOrDefault(conversationDocument, "participants", new ArrayList<>(), String.class));
            conversation.setCallerTCId(getOrDefault(conversationDocument, "callerTCId", "unknown"));
            conversation.setReceiverTCId(getOrDefault(conversationDocument, "receiverTCId", "unknown"));
            conversation.setCallerLastMessage(getOrDefault(conversationDocument, "callerLastMessage", -1));
            conversation.setReceiverLastMessage(getOrDefault(conversationDocument, "receiverLastMessage", -1));
            conversation.setCallerAnonymous(getOrDefault(conversationDocument, "callerAnonymous", false));
            conversation.setReceiverAnonymous(getOrDefault(conversationDocument, "receiverAnonymous", false));
            conversation.setStarted(getOrDefault(conversationDocument, "started", -1));
            conversation.setEnded(getOrDefault(conversationDocument, "ended", -1));
            conversation.setReport(getOrDefault(conversationDocument, "report", false));

            List<TCMessage> messages = new ArrayList<>();
            List<Document> messageDocuments = getOrDefault(conversationDocument, "messages", new ArrayList<>(), Document.class);

            for (Document messageDocument : messageDocuments) {
                TCMessage message = new TCMessage();

                message.setCaller(getOrDefault(messageDocument, "caller", false));
                message.setAuthor(getOrDefault(messageDocument, "author", "unknown"));
                message.setChannel(getOrDefault(messageDocument, "channel", "unknown"));
                message.setContent(getOrDefault(messageDocument, "content", "unknown"));
                message.setFlags(getOrDefault(conversationDocument, "participants", new ArrayList<>(), String.class).toArray(new String[0]));
                message.setSent(getOrDefault(messageDocument, "sent", -1));

                messages.add(message);
            }

            conversation.setMessages(messages);

            return conversation;
        } catch (MongoException me) {
            logger.error("Unable to find Chat: {}", me.getMessage());
        }

        return null;
    }
}
