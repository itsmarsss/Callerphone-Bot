package com.itsmarsss.database.categories;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.callerphone.msginbottle.entities.Page;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.itsmarsss.database.DatabaseUtil.getOrDefault;
import static com.itsmarsss.database.DatabaseUtil.getOrDefaultInt;

public final class MIB {
    public static final Logger logger = LoggerFactory.getLogger(MIB.class);

    private MIB() {
    }

    public static Bottle createMIB(String authorId, String message, boolean signed) {
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();

        try {
            long time = Instant.now().getEpochSecond();
            String mibId = ToolSet.generateUID();

            Document page = new Document()
                    .append("pageNum", 0)
                    .append("author", authorId)
                    .append("message", message)
                    .append("signed", signed)
                    .append("released", time);

            InsertOneResult result = mibCollection.insertOne(new Document()
                    .append("id", mibId)
                    .append("pages", Collections.singletonList(page))
                    .append("created", time));

            if (result.getInsertedId() == null) {
                logger.error("MIB insert returned no id for author {}", authorId);
                return null;
            }

            logger.debug("Created MIB {} by {}", mibId, authorId);
            return new Bottle(mibId, new ArrayList<>(Collections.singletonList(
                    new Page(0, authorId, message, signed, time)
            )));
        } catch (MongoException me) {
            logger.error("Unable to add new MIB: {}", me.getMessage());
            return null;
        }
    }

    public static Bottle findBottle() {
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();

        try {
            List<Document> randomDocument = mibCollection.aggregate(
                            Collections.singletonList(Aggregates.sample(1)))
                    .into(new ArrayList<>());

            if (randomDocument.isEmpty()) {
                return null;
            }
            return parseDocumentToBottle(randomDocument.get(0));
        } catch (MongoException me) {
            logger.error("Unable to find MIB: {}", me.getMessage());
            return null;
        }
    }

    public static Bottle getBottle(String id) {
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();

        try {
            Document mibDocument = mibCollection.find(new Document("id", id)).first();
            if (mibDocument == null) {
                return null;
            }
            return parseDocumentToBottle(mibDocument);
        } catch (MongoException me) {
            logger.error("Unable to get MIB {}: {}", id, me.getMessage());
            return null;
        }
    }

    public static Bottle addMIBPage(String authorId, String message, boolean signed, String mibId) {
        MongoCollection<Document> collection = Callerphone.dbConnector.getMibsCollection();

        try {
            Bottle bottle = getBottle(mibId);
            if (bottle == null) {
                return null;
            }

            int newPageNum = bottle.getPages().size();
            long currentTime = Instant.now().getEpochSecond();

            Document pageDoc = new Document("pageNum", newPageNum)
                    .append("author", authorId)
                    .append("message", message)
                    .append("signed", signed)
                    .append("released", currentTime);

            collection.updateOne(new Document("id", mibId), Updates.push("pages", pageDoc));

            bottle.getPages().add(new Page(newPageNum, authorId, message, signed, currentTime));
            return bottle;
        } catch (MongoException me) {
            logger.error("Unable to update MIB {}: {}", mibId, me.getMessage());
            return null;
        }
    }

    private static Bottle parseDocumentToBottle(Document mibDocument) {
        if (mibDocument == null) {
            return null;
        }

        String id = getOrDefault(mibDocument, "id", "unknown");
        List<Document> pagesDocs = getOrDefault(mibDocument, "pages", new ArrayList<>(), Document.class);
        ArrayList<Page> pages = new ArrayList<>();

        if (pagesDocs != null) {
            for (Document pageDoc : pagesDocs) {
                try {
                    pages.add(new Page(
                            getOrDefaultInt(pageDoc, "pageNum", -1),
                            getOrDefault(pageDoc, "author", "unknown"),
                            getOrDefault(pageDoc, "message", "*No content found*"),
                            getOrDefault(pageDoc, "signed", false),
                            getOrDefault(pageDoc, "released", Instant.now().getEpochSecond())
                    ));
                } catch (Exception e) {
                    logger.error("Error parsing page for MIB {}: {}", id, e.getMessage());
                }
            }
        }
        return new Bottle(id, pages);
    }
}
