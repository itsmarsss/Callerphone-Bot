package com.marsss.database.categories;

import com.marsss.callerphone.Callerphone;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    public static final Logger logger = LoggerFactory.getLogger(Filter.class);

    public static List<String> filter = new ArrayList<>();

    public static void getFilter() {
        MongoCollection<Document> filterCollection = Callerphone.dbConnector.getFilterCollection();

        try {
            Document filterDocument = filterCollection.find(new Document("id", "filter")).first();

            filter = (List<String>) filterDocument.get("filters");
        } catch (MongoException me) {
            logger.error("Unable to update filter: {}", me.getMessage());
        }
    }
}
