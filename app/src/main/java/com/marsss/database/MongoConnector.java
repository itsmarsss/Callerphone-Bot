package com.marsss.database;

import com.marsss.callerphone.Callerphone;
import com.marsss.database.categories.Filter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoConnector {

    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> filtersCollection;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> poolsCollection;
    private MongoCollection<Document> mibsCollection;
    private MongoCollection<Document> chatsCollection;

    public MongoConnector() {
    }

    public boolean init() {
        try {
            ConnectionString connectionString = new ConnectionString(Callerphone.config.getDatabaseURL());

            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();

            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase db = mongoClient.getDatabase("callerphone-bot");

            setMongoDatabase(db);
            setFiltersCollection(db.getCollection("filters"));
            setUsersCollection(db.getCollection("users"));
            setPoolsCollection(db.getCollection("pools"));
            setMibsCollection(db.getCollection("mibs"));
            setChatsCollection(db.getCollection("chats"));

            Filter.getFilter();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public MongoCollection<Document> getFiltersCollection() {
        return filtersCollection;
    }

    public void setFiltersCollection(MongoCollection<Document> filtersCollection) {
        this.filtersCollection = filtersCollection;
    }

    public MongoCollection<Document> getUsersCollection() {
        return usersCollection;
    }

    public void setUsersCollection(MongoCollection<Document> usersCollection) {
        this.usersCollection = usersCollection;
    }

    public MongoCollection<Document> getPoolsCollection() {
        return poolsCollection;
    }

    public void setPoolsCollection(MongoCollection<Document> poolsCollection) {
        this.poolsCollection = poolsCollection;
    }

    public MongoCollection<Document> getMibsCollection() {
        return mibsCollection;
    }

    public void setMibsCollection(MongoCollection<Document> mibsCollection) {
        this.mibsCollection = mibsCollection;
    }

    public MongoCollection<Document> getChatsCollection() {
        return chatsCollection;
    }

    public void setChatsCollection(MongoCollection<Document> chatsCollection) {
        this.chatsCollection = chatsCollection;
    }
}
