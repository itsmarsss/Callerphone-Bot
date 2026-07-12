package com.itsmarsss.database.categories;

import com.itsmarsss.callerphone.Callerphone;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static com.itsmarsss.database.DatabaseUtil.getOrDefault;

/**
 * Cooldown storage with in-memory write-through cache to avoid Mongo on every message.
 */
public class Cooldown {
    public static final Logger logger = LoggerFactory.getLogger(Cooldown.class);

    private static final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    private static String cacheKey(String id, String cooldownType) {
        return id + ":" + cooldownType;
    }

    public static long queryUserCooldown(String id, String cooldownType) {
        String key = cacheKey(id, cooldownType);
        Long cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();
        try {
            Document userDocument = usersCollection.find(new Document("id", id)).first();
            long value = userDocument != null
                    ? getOrDefault(userDocument, "cooldowns_" + cooldownType, 0L)
                    : 0L;
            cache.put(key, value);
            return value;
        } catch (MongoException me) {
            logger.error("Unable to get {} cooldown for user: {}, {}", cooldownType, id, me.getMessage());
            return 0;
        }
    }

    private static void updateUserCooldown(String id, String cooldownType) {
        long now = System.currentTimeMillis();
        cache.put(cacheKey(id, cooldownType), now);

        MongoCollection<Document> usersCollection = Callerphone.dbConnector.getUsersCollection();
        try {
            usersCollection.updateOne(
                    new Document("id", id),
                    new Document("$set", new Document("cooldowns_" + cooldownType, now))
            );
            logger.debug("Updated {} cooldown for user: {}", cooldownType, id);
        } catch (MongoException me) {
            logger.error("Unable to update {} cooldown for user: {}, {}", cooldownType, id, me.getMessage());
        }
    }

    /** Credit grant cooldown for call messages (storage key retained for existing users). */
    public static long getCreditCooldown(String id) {
        return queryUserCooldown(id, "poolChat");
    }

    public static void setCreditCooldown(String id) {
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
