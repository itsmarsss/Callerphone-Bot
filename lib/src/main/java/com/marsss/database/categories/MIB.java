package com.marsss.database.categories;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.entities.Bottle;
import com.marsss.callerphone.msginbottle.entities.Page;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.marsss.database.DatabaseUtil.getOrDefault;

public class MIB {
    public static final Logger logger = LoggerFactory.getLogger(MIB.class);

    public static boolean createMIB(String id, String message, boolean anon) {
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();

        try {
            List<Document> pages = new ArrayList<>();

            long time = Instant.now().getEpochSecond();

            pages.add(new Document()
                    .append("pageNum", 0)
                    .append("author", id)
                    .append("message", message)
                    .append("signed", anon)
                    .append("released", time));

            String mib = ToolSet.generateUID();

            InsertOneResult result = mibCollection.insertOne(new Document()
                    .append("id", mib)
                    .append("pages", pages)
                    .append("created", time));

            if (result.getInsertedId() != null) {
                logger.info("Added new MIB: {}", id);
            } else {
                logger.error("MIB addition not inserted for MIB: {}", id);
            }
            return true;
        } catch (MongoException me) {
            logger.error("Unable to add new MIB: {}", me.getMessage());
        }
        return false;
    }

    public static Bottle findBottle() {
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();

        try {
            List<Document> randomDocument = mibCollection.aggregate(
                            Collections.singletonList(Aggregates.sample(1)))
                    .into(new ArrayList<>());

            if (!randomDocument.isEmpty()) {
                return parseDocumentToBottle(randomDocument.get(0));
            }
            return null;
        } catch (MongoException me) {
            logger.error("Unable to find MIB: {}", me.getMessage());
        }
        return null;
    }

    public static Bottle getBottle(String id) {
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();

        try {
            Document mibDocument = mibCollection.find(new Document("id", id)).first();

            logger.info("MIB: {}", id);
            return parseDocumentToBottle(mibDocument);
        } catch (MongoException me) {
            logger.error("Unable to get MIB {}: {}", id, me.getMessage());
            return null;
        }
    }


    public static boolean addMIBPage(String id, String message, boolean anon, String mibId) {
        MongoCollection<Document> collection = Callerphone.dbConnector.getMibsCollection();

        try {
            Bottle bottle = getBottle(mibId);

            if (bottle == null) {
                return false;
            }

            int newPageNum = bottle.getPages().size();
            long currentTime = Instant.now().getEpochSecond();

            Page newPage = new Page(newPageNum, id, message, anon, currentTime);

            bottle.getPages().add(newPage);

            ArrayList<Document> updatedPages = new ArrayList<>();
            for (Page page : bottle.getPages()) {
                Document pageDoc = new Document("pageNum", page.getPageNum())
                        .append("author", page.getAuthor())
                        .append("message", page.getMessage())
                        .append("signed", page.isSigned())
                        .append("released", page.getReleased());
                updatedPages.add(pageDoc);
            }

            collection.updateOne(new Document("id", mibId), new Document("$set", new Document("pages", updatedPages)));

            return true;
        } catch (MongoException me) {
            logger.error("Unable to update MIB {}: {}", mibId, me.getMessage());
            return false;
        }
    }


    private static Bottle parseDocumentToBottle(Document mibDocument) {
        String id = getOrDefault(mibDocument, "id", "unknown");

        List<Document> pagesDocs = getOrDefault(mibDocument, "messages", new ArrayList<>(), Document.class);
        ArrayList<Page> pages = new ArrayList<>();

        if (pagesDocs != null) {
            for (Document pageDoc : pagesDocs) {
                try {
                    int pageNum = (int) getOrDefault(pageDoc, "pageNum", Integer.MAX_VALUE);
                    String author = getOrDefault(pageDoc, "author", "unknown");
                    String message = getOrDefault(pageDoc, "message", "*No content found*");
                    boolean signed = getOrDefault(pageDoc, "signed", false);
                    long released = getOrDefault(pageDoc, "released", Instant.now().getEpochSecond());

                    pages.add(new Page(pageNum, author, message, signed, released));
                } catch (Exception e) {
                    logger.error("Error parsing page id {}: {}", id, e.getMessage());
                }
            }
        }

        return new Bottle(id, pages);
    }
}
