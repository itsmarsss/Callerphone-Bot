package com.marsss.database.categories;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.users.BotUser;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Users {
    public static final Logger logger = LoggerFactory.getLogger(Users.class);

    public static void createUser(String id) {
        try {
            InsertOneResult result = Callerphone.dbConnector.getUsersCollection().insertOne(new Document()
                    .append("_id", new ObjectId())
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

            if (result.wasAcknowledged()) {
                logger.info("Added new user: {}", id);
            } else {
                logger.error("User addition not acknowledged for user: {}", id);
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
                createUser(id);
                usersCollection.updateOne(
                        new Document("id", id),
                        new Document("$inc", new Document(field, amount))
                );
            }

            logger.info("User: {} updated {} by: {}", id, field, amount);
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
                createUser(id);
                usersCollection.updateOne(
                        new Document("id", id),
                        new Document("$set", new Document(field, value))
                );
            }

            logger.info("User: {} updated {} to: {}", id, field, value);
        } catch (MongoException me) {
            logger.error("Unable to update {} for user: {}, {}", field, id, me.getMessage());
        }
    }

    private static long queryUserFieldLong(String id, String field) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();

            if (userDocument == null) {
                createUser(id);
                return 0;
            }

            logger.info("User: {} got {}", id, field);
            return userDocument.getLong(field);
        } catch (MongoException me) {
            logger.error("Unable to get {} for user: {}, {}", field, id, me.getMessage());
            return 0;
        }
    }

    private static String queryUserFieldString(String id, String field) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();

            if (userDocument == null) {
                createUser(id);
                return "";
            }

            return userDocument.getString(field);
        } catch (MongoException me) {
            logger.error("Unable to get {} for user: {}, {}", field, id, me.getMessage());
            return "";
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
        return queryUserFieldString(id, "prefix");
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
        return queryUserFieldString(id, "status").equals("blacklisted");
    }

    public static boolean isModerator(String id) {
        return queryUserFieldString(id, "status").equals("moderator");
    }

    public static void addBlacklist(String id) {
        updateUserFieldString(id, "status", "blacklisted");
    }

    public static void addAdmin(String id) {
        updateUserFieldString(id, "status", "moderator");
    }

    public static void addUser(String id) {
        updateUserFieldString(id, "status", "user");
    }

    public static boolean hasPrefix(String id) {
        String prefix = queryUserFieldString(id, "prefix");
        return prefix != null && !prefix.isEmpty();
    }

    public static String getReason(String id) {
        return queryUserFieldString(id, "reason");
    }

    public static String getUserStatus(String id) {
        if (isModerator(id)) {
            return "Moderator";
        } else if (isBlacklisted(id)) {
            return "Blacklisted | Reason: " + getReason(id);
        }
        return "User";
    }

    public static boolean hasUser(String id) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();
            boolean exists = userDocument != null;

            if (!exists) {
                createUser(id);
            }

            logger.info("User existence check for {}: {}", id, exists);
            return exists;
        } catch (MongoException me) {
            logger.error("Unable to check existence for user: {}, {}", id, me.getMessage());
            return false;
        }
    }

    ////////////////////////////
    public static final HashMap<String, BotUser> users = new HashMap<>();
    public static BotUser getUser(String id) {
        if (!users.containsKey(id)) {
            createUser(id);
        }
        return users.get(id);
    }
}
