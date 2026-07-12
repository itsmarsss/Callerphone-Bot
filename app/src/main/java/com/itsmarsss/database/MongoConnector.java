package com.itsmarsss.database;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.database.categories.Filter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoConnector {
    private static final Logger logger = LoggerFactory.getLogger(MongoConnector.class);
    private static final String DATABASE_NAME = "callerphone-bot";

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> filtersCollection;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> mibsCollection;
    private MongoCollection<Document> chatsCollection;

    public boolean init() {
        try {
            ConnectionString connectionString = new ConnectionString(Callerphone.config.getDatabaseURL());
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

            mongoClient = MongoClients.create(settings);
            mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);

            filtersCollection = mongoDatabase.getCollection("filters");
            usersCollection = mongoDatabase.getCollection("users");
            mibsCollection = mongoDatabase.getCollection("mibs");
            chatsCollection = mongoDatabase.getCollection("chats");

            Filter.getFilter();
            logger.info("Connected to MongoDB database '{}'", DATABASE_NAME);
            return true;
        } catch (Exception e) {
            logger.error("Failed to connect to MongoDB", e);
            return false;
        }
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public MongoCollection<Document> getFiltersCollection() {
        return filtersCollection;
    }

    public MongoCollection<Document> getUsersCollection() {
        return usersCollection;
    }

    public MongoCollection<Document> getMibsCollection() {
        return mibsCollection;
    }

    public MongoCollection<Document> getChatsCollection() {
        return chatsCollection;
    }
}
