package com.marsss.database;

import com.marsss.callerphone.Callerphone;
import com.marsss.database.categories.Filter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoConnector {

    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> filterCollection;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> poolsCollection;
    private MongoCollection<Document> mibCollection;
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
            setFilterCollection(db.getCollection("filter"));
            setUsersCollection(db.getCollection("users"));
            setPoolsCollection(db.getCollection("pools"));
            setMibCollection(db.getCollection("mib"));
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

    public MongoCollection<Document> getFilterCollection() {
        return filterCollection;
    }

    public void setFilterCollection(MongoCollection<Document> filterCollection) {
        this.filterCollection = filterCollection;
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

    public MongoCollection<Document> getMibCollection() {
        return mibCollection;
    }

    public void setMibCollection(MongoCollection<Document> mibCollection) {
        this.mibCollection = mibCollection;
    }

    public MongoCollection<Document> getChatsCollection() {
        return chatsCollection;
    }

    public void setChatsCollection(MongoCollection<Document> chatsCollection) {
        this.chatsCollection = chatsCollection;
    }
}
