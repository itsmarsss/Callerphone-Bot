package com.itsmarsss.callerphone.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lightweight migration ledger for Match schema only.
 */
public final class MigrationRunner {
    private static final Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

    private final MongoCollection<Document> migrations;

    public MigrationRunner(MongoDatabase database) {
        this.migrations = database.getCollection(MatchCollections.SCHEMA_MIGRATIONS);
    }

    public void run(List<Migration> ordered) {
        for (Migration migration : ordered) {
            if (migrations.find(Filters.eq("id", migration.id())).first() != null) {
                continue;
            }
            logger.info("Applying migration {}", migration.id());
            migration.apply();
            migrations.insertOne(new Document("id", migration.id())
                    .append("appliedAt", Date.from(Instant.now()))
                    .append("description", migration.description()));
        }
    }

    public record Migration(String id, String description, Runnable apply) {
        public static Migration of(String id, String description, Consumer<Void> body) {
            return new Migration(id, description, () -> body.accept(null));
        }
    }
}
