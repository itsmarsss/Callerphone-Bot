package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight periodic cleanup for Match only.
 * - Expires stale connect requests
 * - Archives long-idle mediated chats
 * - Scrubs message content past retention (keeps metadata for reports if needed later)
 */
public final class MatchMaintenanceJobs {
    private static final Logger logger = LoggerFactory.getLogger(MatchMaintenanceJobs.class);
    private static final Duration IDLE_ARCHIVE = Duration.ofDays(14);
    private static final Duration MESSAGE_RETENTION = Duration.ofDays(30);

    private final MongoDatabase database;
    private final ScheduledExecutorService scheduler;

    public MatchMaintenanceJobs(MongoDatabase database) {
        this.database = database;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "match-maintenance");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::safeRun, 2, 15, TimeUnit.MINUTES);
        logger.info("Match maintenance jobs scheduled");
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void safeRun() {
        try {
            expireConnectRequests();
            archiveIdleConversations();
            scrubOldMessageContent();
        } catch (Exception e) {
            logger.warn("Match maintenance tick failed: {}", e.getMessage());
        }
    }

    private void expireConnectRequests() {
        MongoCollection<Document> col = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
        Date now = Date.from(Instant.now());
        var result = col.updateMany(
                Filters.and(
                        Filters.eq("stage", ConversationStage.CONNECT_PENDING.name()),
                        Filters.lt("connectExpiresAt", now)
                ),
                Updates.combine(
                        Updates.set("stage", ConversationStage.MEDIATED.name()),
                        Updates.unset("connectRequestedBy"),
                        Updates.unset("connectRequestedAt"),
                        Updates.unset("connectExpiresAt")
                )
        );
        if (result.getModifiedCount() > 0) {
            logger.info("Expired {} connect requests", result.getModifiedCount());
        }
    }

    private void archiveIdleConversations() {
        MongoCollection<Document> col = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
        Date cutoff = Date.from(Instant.now().minus(IDLE_ARCHIVE));
        var result = col.updateMany(
                Filters.and(
                        Filters.ne("stage", ConversationStage.ARCHIVED.name()),
                        Filters.lt("lastActivityAt", cutoff)
                ),
                Updates.combine(
                        Updates.set("stage", ConversationStage.ARCHIVED.name()),
                        Updates.set("archivedAt", Date.from(Instant.now()))
                )
        );
        if (result.getModifiedCount() > 0) {
            logger.info("Archived {} idle Match conversations", result.getModifiedCount());
        }
    }

    private void scrubOldMessageContent() {
        MongoCollection<Document> col = database.getCollection(MatchCollections.MATCH_MESSAGES);
        Date cutoff = Date.from(Instant.now().minus(MESSAGE_RETENTION));
        // Only scrub content for messages whose conversation is archived and older than retention
        MongoCollection<Document> conversations = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
        for (Document conv : conversations.find(Filters.and(
                Filters.eq("stage", ConversationStage.ARCHIVED.name()),
                Filters.lt("archivedAt", cutoff)
        )).limit(200)) {
            String conversationId = conv.getString("_id");
            col.updateMany(
                    Filters.and(
                            Filters.eq("conversationId", conversationId),
                            Filters.ne("content", "[redacted]")
                    ),
                    Updates.set("content", "[redacted]")
            );
        }
    }
}
