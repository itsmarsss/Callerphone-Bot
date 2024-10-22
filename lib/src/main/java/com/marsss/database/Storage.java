package com.marsss.database;

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

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

public class Storage {
    public static final Logger logger = LoggerFactory.getLogger(Storage.class);

    public static final LinkedList<String> filter = new LinkedList<>();

    private static void getFilter(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                filter.add(line);
            }
        }
    }

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
        return queryUserStatus(id, "blacklisted");
    }

    public static boolean isModerator(String id) {
        return queryUserStatus(id, "moderator");
    }

    private static boolean queryUserStatus(String id, String statusToCheck) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();

            if (userDocument == null) {
                createUser(id);
                return false;
            }

            String status = userDocument.getString("status");

            logger.info("Check {} status for user: {}", statusToCheck, id);
            return statusToCheck.equals(status);
        } catch (MongoException me) {
            logger.error("Unable to check status for user: {}, {}", id, me.getMessage());
            return false;
        }
    }

    public static void addBlacklist(String id) {
        updateUserStatus(id, "blacklisted");
    }

    public static void addAdmin(String id) {
        updateUserStatus(id, "moderator");
    }

    public static void addUser(String id) {
        updateUserStatus(id, "user");
    }

    private static void updateUserStatus(String id, String newStatus) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            usersCollection.updateOne(
                    new Document("id", id),
                    new Document("$set", new Document("status", newStatus))
            );
            logger.info("User status updated: {} -> {}", id, newStatus);
        } catch (MongoException me) {
            logger.error("Unable to update status for user: {}, {}", id, me.getMessage());
        }
    }

    public static boolean hasPrefix(String id) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();

            if (userDocument == null) {
                createUser(id);
                return false;
            }

            String prefix = userDocument.getString("prefix");
            return prefix != null && !prefix.isEmpty();
        } catch (MongoException me) {
            logger.error("Unable to check prefix for user: {}, {}", id, me.getMessage());
            return false;
        }
    }

    public static String getReason(String id) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();

            if (userDocument == null) {
                createUser(id);
                return "";
            }

            return userDocument.getString("reason");
        } catch (MongoException me) {
            logger.error("Unable to get reason for user: {}, {}", id, me.getMessage());
            return "";
        }
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

    public static long queryUserCooldown(String id, String cooldownType) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();
            if (userDocument != null) {
                return userDocument.getLong("cooldowns_" + cooldownType);
            } else {
                createUser(id);
                return 0;
            }
        } catch (MongoException me) {
            logger.error("Unable to get {} cooldown for user: {}, {}", cooldownType, id, me.getMessage());
            return 0;
        }
    }

    private static void updateUserCooldown(String id, String cooldownType) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            usersCollection.updateOne(
                    new Document("id", id),
                    new Document("$set", new Document("cooldowns_" + cooldownType, System.currentTimeMillis()))
            );
            logger.info("Updated {} cooldown for user: {}", cooldownType, id);
        } catch (MongoException me) {
            logger.error("Unable to update {} cooldown for user: {}, {}", cooldownType, id, me.getMessage());
        }
    }

    public static long queryUserCooldown(String id) {
        return queryUserCooldown(id, "poolChat");
    }

    public static void updateUserCooldown(String id) {
        updateUserCooldown(id, "poolChat");
    }

    public static long getCmdCooldown(String id) {
        return queryUserCooldown(id, "command");
    }

    public static void updateCmdCooldown(String id) {
        updateUserCooldown(id, "command");
    }

    public static long getMIBSendCoolDown(String id) {
        return queryUserCooldown(id, "MIBSend");
    }

    public static void updateMIBSendCoolDown(String id) {
        updateUserCooldown(id, "MIBSend");
    }

    public static long getMIBFindCoolDown(String id) {
        return queryUserCooldown(id, "MIBFind");
    }

    public static void updateMIBFindCoolDown(String id) {
        updateUserCooldown(id, "MIBFind");
    }



    public static final HashMap<String, BotUser> users = new HashMap<>();
    public static BotUser getUser(String id) {
        if (!users.containsKey(id)) {
            createUser(id);
        }
        return users.get(id);
    }
}
