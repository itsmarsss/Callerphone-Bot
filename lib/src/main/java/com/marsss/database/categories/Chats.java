package com.marsss.database.categories;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.entities.ConversationStorage;
import com.marsss.callerphone.tccallerphone.entities.MessageStorage;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chats {
    public static final Logger logger = LoggerFactory.getLogger(Chats.class);

    public static boolean createChat(ConversationStorage convo) {
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
            for (MessageStorage message : convo.getMessages()) {
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

    public static ConversationStorage queryChat(String id) {
        MongoCollection<Document> chatsCollection = Callerphone.dbConnector.getChatsCollection();

        try {
            Document chatDocument = chatsCollection.find(new Document("id", id)).first();

            ConversationStorage conversation = new ConversationStorage();

            conversation.setId(chatDocument.containsKey("id") ? chatDocument.getString("id") : "unknown");
            conversation.setParticipants(chatDocument.containsKey("participants") ? chatDocument.getList("participants", String.class) : new ArrayList<>());
            conversation.setCallerTCId(chatDocument.containsKey("callerTCId") ? chatDocument.getString("callerTCId") : "-1");
            conversation.setReceiverTCId(chatDocument.containsKey("receiverTCId") ? chatDocument.getString("receiverTCId") : "-1");
            conversation.setCallerLastMessage(chatDocument.containsKey("callerLastMessage") ? chatDocument.getLong("callerLastMessage") : -1);
            conversation.setReceiverLastMessage(chatDocument.containsKey("receiverLastMessage") ? chatDocument.getLong("receiverLastMessage") : -1);
            conversation.setCallerAnonymous(chatDocument.containsKey("callerAnonymous") ? chatDocument.getBoolean("callerAnonymous") : false);
            conversation.setReceiverAnonymous(chatDocument.containsKey("receiverAnonymous") ? chatDocument.getBoolean("receiverAnonymous") : false);
            conversation.setStarted(chatDocument.containsKey("started") ? chatDocument.getLong("started") : -1);
            conversation.setEnded(chatDocument.containsKey("ended") ? chatDocument.getLong("ended") : -1);
            conversation.setReport(chatDocument.containsKey("report") ? chatDocument.getBoolean("report") : false);

            List<MessageStorage> messages = new ArrayList<>();
            List<Document> messageDocuments = chatDocument.getList("messages", Document.class);

            for (Document messageDocument : messageDocuments) {
                MessageStorage message = new MessageStorage();

                message.setCaller(messageDocument.containsKey("caller") ? messageDocument.getBoolean("caller") : false);
                message.setAuthor(messageDocument.containsKey("author") ? messageDocument.getString("author") : "-1");
                message.setChannel(messageDocument.containsKey("channel") ? messageDocument.getString("channel") : "-1");
                message.setContent(messageDocument.containsKey("content") ? messageDocument.getString("content") : "unknown");
                message.setFlags(messageDocument.containsKey("flags") ? messageDocument.getList("flags", String.class).toArray(new String[0]) : new String[0]);
                message.setSent(messageDocument.containsKey("sent") ? messageDocument.getLong("sent") : -1);

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
