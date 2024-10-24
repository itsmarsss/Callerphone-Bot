package com.marsss.database.categories;

import com.marsss.callerphone.Callerphone;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cooldown {
    public static final Logger logger = LoggerFactory.getLogger(Cooldown.class);

    public static long queryUserCooldown(String id, String cooldownType) {
        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();

        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();
            if (userDocument != null) {
                return userDocument.getLong("cooldowns_" + cooldownType);
            } else {
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

    public static long getPoolCooldown(String id) {
        return queryUserCooldown(id, "poolChat");
    }

    public static void setUserCooldown(String id) {
        updateUserCooldown(id, "poolChat");
    }

    public static long getCmdCooldown(String id) {
        return queryUserCooldown(id, "command");
    }

    public static void setCmdCooldown(String id) {
        updateUserCooldown(id, "command");
    }

    public static long getMIBSendCoolDown(String id) {
        return queryUserCooldown(id, "MIBSend");
    }

    public static void setMIBSendCoolDown(String id) {
        updateUserCooldown(id, "MIBSend");
    }

    public static long getMIBFindCoolDown(String id) {
        return queryUserCooldown(id, "MIBFind");
    }

    public static void setMIBFindCoolDown(String id) {
        updateUserCooldown(id, "MIBFind");
    }
}
