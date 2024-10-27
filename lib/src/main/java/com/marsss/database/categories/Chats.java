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
            logger.error("Unable to find MIB: {}", me.getMessage());
        }
        return false;
    }
}
