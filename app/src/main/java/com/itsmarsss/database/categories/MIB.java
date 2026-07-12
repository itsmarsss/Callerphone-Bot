package com.itsmarsss.database.categories;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.callerphone.msginbottle.entities.Page;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Bottles the user has participated in (authored any page), newest first.
     * Multi-page bottles are preferred as "active threads".
     */
    public static List<Bottle> findThreadsForUser(String userId, int limit) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();
        List<Bottle> out = new ArrayList<>();
        try {
            for (Document doc : mibCollection.find(Filters.eq("pages.author", userId))
                    .sort(Sorts.descending("created"))
                    .limit(Math.max(1, Math.min(limit, 50)))) {
                Bottle bottle = parseDocumentToBottle(doc);
                if (bottle != null && bottle.getPages() != null && !bottle.getPages().isEmpty()) {
                    out.add(bottle);
                }
            }
        } catch (MongoException me) {
            logger.error("Unable to list MIB threads for {}: {}", userId, me.getMessage());
        }
        return out;
    }

    public static List<Bottle> getBottles(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        MongoCollection<Document> mibCollection = Callerphone.dbConnector.getMibsCollection();
        List<Bottle> out = new ArrayList<>();
        try {
            for (Document doc : mibCollection.find(Filters.in("id", ids))) {
                Bottle bottle = parseDocumentToBottle(doc);
                if (bottle != null) {
                    out.add(bottle);
                }
            }
        } catch (MongoException me) {
            logger.error("Unable to load MIBs by id: {}", me.getMessage());
        }
        return out;
    }

    /** Unique participant user ids for a bottle (page authors). */
    public static Set<String> participantIds(Bottle bottle) {
        Set<String> ids = new HashSet<>();
        if (bottle == null || bottle.getPages() == null) {
            return ids;
        }
        for (Page page : bottle.getPages()) {
            if (page.getAuthor() != null && !page.getAuthor().isBlank() && !"unknown".equals(page.getAuthor())) {
                ids.add(page.getAuthor());
            }
        }
        return ids;
    }

    public static String preview(Bottle bottle, int maxLen) {
        if (bottle == null || bottle.getPages() == null || bottle.getPages().isEmpty()) {
            return "Empty bottle";
        }
        String msg = bottle.getPages().get(0).getMessage();
        if (msg == null) {
            return "Empty bottle";
        }
        msg = msg.replace('\n', ' ').trim();
        if (msg.length() <= maxLen) {
            return msg;
        }
        return msg.substring(0, Math.max(1, maxLen - 1)) + "…";
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
