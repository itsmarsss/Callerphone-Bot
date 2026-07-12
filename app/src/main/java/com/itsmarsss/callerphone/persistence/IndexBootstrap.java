package com.itsmarsss.callerphone.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates Match indexes on new collections only. Never drops or rewrites legacy data.
 */
public final class IndexBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(IndexBootstrap.class);

    private final MongoDatabase database;

    public IndexBootstrap(MongoDatabase database) {
        this.database = database;
    }

    public void bootstrap() {
        logger.info("Bootstrapping Match collection indexes");

        collection(MatchCollections.MATCH_USERS)
                .createIndex(Indexes.ascending("_id"));
        collection(MatchCollections.MATCH_USERS)
                .createIndex(Indexes.ascending("enrolled", "ageCohort"));

        collection(MatchCollections.MATCH_PROFILES)
                .createIndex(Indexes.ascending("state", "lastActiveAt"));
        collection(MatchCollections.MATCH_PROFILES)
                .createIndex(Indexes.ascending("ageCohort", "state", "lastActiveAt"));

        collection(MatchCollections.MATCH_DECISIONS)
                .createIndex(Indexes.ascending("viewerId", "subjectId"), new IndexOptions().unique(true));
        collection(MatchCollections.MATCH_DECISIONS)
                .createIndex(Indexes.ascending("viewerId", "createdAt"));
        collection(MatchCollections.MATCH_DECISIONS)
                .createIndex(Indexes.ascending("expiresAt"), new IndexOptions().expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS));

        collection(MatchCollections.MATCHES)
                .createIndex(Indexes.ascending("pairKey"), new IndexOptions().unique(true));
        collection(MatchCollections.MATCHES)
                .createIndex(Indexes.ascending("userIds", "status", "matchedAt"));

        collection(MatchCollections.MATCH_CONVERSATIONS)
                .createIndex(Indexes.ascending("matchId"), new IndexOptions().unique(true));
        collection(MatchCollections.MATCH_CONVERSATIONS)
                .createIndex(Indexes.ascending("participants", "stage", "lastActivityAt"));
        collection(MatchCollections.MATCH_CONVERSATIONS)
                .createIndex(Indexes.ascending("connectExpiresAt", "stage"));

        collection(MatchCollections.MATCH_MESSAGES)
                .createIndex(Indexes.ascending("conversationId", "createdAt"));
        collection(MatchCollections.MATCH_MESSAGES)
                .createIndex(Indexes.ascending("createdAt"));

        collection(MatchCollections.BLOCKS)
                .createIndex(Indexes.ascending("blockerId", "blockedId", "product"), new IndexOptions().unique(true));
        collection(MatchCollections.REPORTS)
                .createIndex(Indexes.ascending("status", "priority", "createdAt"));
        collection(MatchCollections.SANCTIONS)
                .createIndex(Indexes.ascending("userId", "product", "active"));
        collection(MatchCollections.CONSENT_EVENTS)
                .createIndex(Indexes.ascending("userId", "type", "createdAt"));
        collection(MatchCollections.AUDIT_EVENTS)
                .createIndex(Indexes.ascending("createdAt"));
        collection(MatchCollections.BROWSE_SESSIONS)
                .createIndex(Indexes.ascending("expiresAt"), new IndexOptions().expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS));
        collection(MatchCollections.PRODUCT_EVENTS)
                .createIndex(Indexes.ascending("createdAt"));
        collection(MatchCollections.SOCIAL_INBOX)
                .createIndex(Indexes.ascending("userId", "priority", "occurredAt"));
        collection(MatchCollections.SOCIAL_INBOX)
                .createIndex(Indexes.ascending("userId", "unread"));
        collection(MatchCollections.SCHEMA_MIGRATIONS)
                .createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));

        logger.info("Match indexes ready");
    }

    private MongoCollection<Document> collection(String name) {
        return database.getCollection(name);
    }
}
