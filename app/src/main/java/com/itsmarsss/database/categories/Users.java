package com.itsmarsss.database.categories;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.users.BotUser;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.itsmarsss.database.DatabaseUtil.getOrDefault;

public class Users {
    public static final Logger logger = LoggerFactory.getLogger(Users.class);

    private static final ConcurrentHashMap<String, CachedUser> cache = new ConcurrentHashMap<>();

    private static final class CachedUser {
        final boolean exists;
        final String status;
        final String prefix;
        final String reason;

        CachedUser(boolean exists, String status, String prefix, String reason) {
            this.exists = exists;
            this.status = status;
            this.prefix = prefix;
            this.reason = reason;
        }
    }

    private static CachedUser loadUser(String id) {
        CachedUser cached = cache.get(id);
        if (cached != null) {
            return cached;
        }

        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();
        try {
            Document doc = usersCollection.find(new Document("id", id)).first();
            CachedUser loaded = doc == null
                    ? new CachedUser(false, "", "", "")
                    : new CachedUser(
                    true,
                    getOrDefault(doc, "status", "user"),
                    getOrDefault(doc, "prefix", ""),
                    getOrDefault(doc, "reason", "")
            );
            cache.put(id, loaded);
            return loaded;
        } catch (MongoException me) {
            logger.error("Unable to load user {}: {}", id, me.getMessage());
            return new CachedUser(false, "", "", "");
        }
    }

    private static void invalidate(String id) {
        cache.remove(id);
    }

    public static void createUser(String id) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            InsertOneResult result = usersCollection.insertOne(new Document()
                    .append("id", id)
                    .append("status", "user")
                    .append("prefix", "")
                    .append("reason", "")
                    .append("credits", 0L)
                    .append("executed", 0L)
                    .append("transmitted", 0L)
                    .append("cooldowns_command", 0L)
                    .append("cooldowns_poolChat", 0L)
                    .append("cooldowns_MIBSend", 0L)
                    .append("cooldowns_MIBFind", 0L));

            if (result.getInsertedId() != null) {
                cache.put(id, new CachedUser(true, "user", "", ""));
                logger.info("Added new user: {}", id);
            } else {
                logger.error("User addition not inserted for user: {}", id);
            }
        } catch (MongoException me) {
            logger.error("Unable to add new user: {}", me.getMessage());
        }
    }

    private static void updateUserFieldLong(String id, String field, long amount) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            UpdateResult result = usersCollection.updateOne(
                    new Document("id", id),
                    new Document("$inc", new Document(field, amount))
            );

            if (result.getMatchedCount() == 0) {
                usersCollection.updateOne(
                        new Document("id", id),
                        new Document("$inc", new Document(field, amount))
                );
            }

            logger.debug("User: {} updated {} by: {}", id, field, amount);
        } catch (MongoException me) {
            logger.error("Unable to update {} for user: {}, {}", field, id, me.getMessage());
        }
    }

    private static void updateUserFieldString(String id, String field, String value) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            UpdateResult result = usersCollection.updateOne(
                    new Document("id", id),
                    new Document("$set", new Document(field, value))
            );

            if (result.getMatchedCount() == 0) {
                usersCollection.updateOne(
                        new Document("id", id),
                        new Document("$set", new Document(field, value))
                );
            }

            invalidate(id);
            logger.debug("User: {} updated {} to: {}", id, field, value);
        } catch (MongoException me) {
            logger.error("Unable to update {} for user: {}, {}", field, id, me.getMessage());
        }
    }

    private static long queryUserFieldLong(String id, String field) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();
            if (userDocument == null) {
                return 0;
            }
            return getOrDefault(userDocument, field, 0L);
        } catch (MongoException me) {
            logger.error("Unable to get {} for user: {}, {}", field, id, me.getMessage());
            return 0;
        }
    }

    public static void reward(String id, long amount) {
        updateUserFieldLong(id, "credits", amount);
    }

    public static void addExecute(String id, int amount) {
        updateUserFieldLong(id, "executed", amount);
    }

    public static void addTransmit(String id, int amount) {
        updateUserFieldLong(id, "transmitted", amount);
    }

    public static long getUserStat(String id, String stat) {
        return queryUserFieldLong(id, stat);
    }

    public static void setPrefix(String id, String prefix) {
        updateUserFieldString(id, "prefix", prefix);
    }

    public static String getPrefix(String id) {
        return loadUser(id).prefix;
    }

    public static long getCredits(String id) {
        return queryUserFieldLong(id, "credits");
    }

    public static long getExecuted(String id) {
        return queryUserFieldLong(id, "executed");
    }

    public static long getTransmitted(String id) {
        return queryUserFieldLong(id, "transmitted");
    }

    public static boolean isBlacklisted(String id) {
        return "blacklisted".equals(loadUser(id).status);
    }

    public static boolean isModerator(String id) {
        return "moderator".equals(loadUser(id).status);
    }

    public static void addBlacklist(String id) {
        updateUserFieldString(id, "status", "blacklisted");
    }

    public static void addModerator(String id) {
        updateUserFieldString(id, "status", "moderator");
    }

    public static void addUser(String id) {
        updateUserFieldString(id, "status", "user");
    }

    public static boolean hasPrefix(String id) {
        String prefix = getPrefix(id);
        return prefix != null && !prefix.isEmpty();
    }

    public static String getReason(String id) {
        return loadUser(id).reason;
    }

    public static String getUserStatus(String id) {
        CachedUser user = loadUser(id);
        if ("moderator".equals(user.status)) {
            return "Moderator";
        }
        if ("blacklisted".equals(user.status)) {
            return "Blacklisted | Reason: " + user.reason;
        }
        return "User";
    }

    public static boolean hasUser(String id) {
        return loadUser(id).exists;
    }

    /**
     * Top users by credits (descending). Each document has {@code id} and {@code credits}.
     */
    public static List<Document> topByCredits(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 25));
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();
        try {
            return usersCollection.aggregate(Arrays.asList(
                    Aggregates.match(Filters.and(
                            Filters.exists("credits", true),
                            Filters.gt("credits", 0)
                    )),
                    Aggregates.sort(Sorts.descending("credits")),
                    Aggregates.limit(safeLimit),
                    Aggregates.project(Projections.fields(
                            Projections.include("id", "credits", "prefix"),
                            Projections.excludeId()
                    ))
            )).into(new ArrayList<>());
        } catch (MongoException me) {
            logger.error("Unable to load credits leaderboard: {}", me.getMessage());
            return new ArrayList<>();
        }
    }

    /** In-memory session state (minigames, etc.). */
    public static final ConcurrentHashMap<String, BotUser> users = new ConcurrentHashMap<>();

    public static BotUser getUser(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return users.computeIfAbsent(id, BotUser::new);
    }
}
