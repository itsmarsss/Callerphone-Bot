package com.marsss.database;

import com.marsss.callerphone.Callerphone;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class MongoConnector {

    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> poolsCollection;

    public MongoConnector() {}

    public boolean init() {
        try{
            ConnectionString connectionString = new ConnectionString(Callerphone.config.getDatabaseURL());

            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();

            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase db = mongoClient.getDatabase("callerphone-bot");

            setMongoDatabase(db);
            setUsersCollection(db.getCollection("users"));
            setPoolsCollection(db.getCollection("pools"));
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
}
