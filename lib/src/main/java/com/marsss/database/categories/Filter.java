package com.marsss.database.categories;

import com.marsss.callerphone.Callerphone;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.marsss.database.DatabaseUtil.getOrDefault;

public class Filter {
    public static final Logger logger = LoggerFactory.getLogger(Filter.class);

    public static List<String> containsfilter = new ArrayList<>();
    public static List<String> wordsfilter = new ArrayList<>();

    public static void getFilter() {
        MongoCollection<Document> filterCollection = Callerphone.dbConnector.getFiltersCollection();

        try {
            Document containsFilterDocument = filterCollection.find(new Document("id", "contains")).first();
            containsfilter = getOrDefault(containsFilterDocument, "filters", new ArrayList<>(), String.class);

            Document wordsFilterDocument = filterCollection.find(new Document("id", "words")).first();
            wordsfilter = getOrDefault(wordsFilterDocument, "filters", new ArrayList<>(), String.class);
        } catch (MongoException me) {
            logger.error("Unable to update filters: {}", me.getMessage());
        }
    }
}
