package com.itsmarsss.database.categories;

import com.itsmarsss.callerphone.Callerphone;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static com.itsmarsss.database.DatabaseUtil.getOrDefault;

/**
 * Profanity / content filters loaded from MongoDB with precompiled patterns.
 */
public final class Filter {
    public static final Logger logger = LoggerFactory.getLogger(Filter.class);

    /** Raw contain-style filters (substring, case-insensitive). */
    public static volatile List<String> containsfilter = Collections.emptyList();
    /** Raw word filters. */
    public static volatile List<String> wordsfilter = Collections.emptyList();

    /** Compiled case-insensitive substring patterns for contains filters. */
    private static volatile List<Pattern> containsPatterns = Collections.emptyList();
    /** Compiled spaced-letter patterns for word filters. */
    private static volatile List<Pattern> wordPatterns = Collections.emptyList();

    private Filter() {
    }

    public static void getFilter() {
        MongoCollection<Document> filterCollection = Callerphone.dbConnector.getFiltersCollection();

        try {
            Document containsFilterDocument = filterCollection.find(new Document("id", "contains")).first();
            Document wordsFilterDocument = filterCollection.find(new Document("id", "words")).first();

            List<String> contains = getOrDefault(containsFilterDocument, "filters", new ArrayList<>(), String.class);
            List<String> words = getOrDefault(wordsFilterDocument, "filters", new ArrayList<>(), String.class);

            containsfilter = new CopyOnWriteArrayList<>(contains);
            wordsfilter = new CopyOnWriteArrayList<>(words);
            rebuildPatterns();
            logger.info("Loaded filters: {} contains, {} words", contains.size(), words.size());
        } catch (MongoException me) {
            logger.error("Unable to update filters: {}", me.getMessage());
        }
    }

    public static void rebuildPatterns() {
        List<Pattern> contains = new ArrayList<>(containsfilter.size());
        for (String ftr : containsfilter) {
            if (ftr == null || ftr.isEmpty()) {
                continue;
            }
            contains.add(Pattern.compile(Pattern.quote(ftr), Pattern.CASE_INSENSITIVE));
        }

        List<Pattern> words = new ArrayList<>(wordsfilter.size());
        for (String ftr : wordsfilter) {
            if (ftr == null || ftr.isEmpty()) {
                continue;
            }
            words.add(Pattern.compile(buildSpacedWordRegex(ftr), Pattern.CASE_INSENSITIVE));
        }

        containsPatterns = Collections.unmodifiableList(contains);
        wordPatterns = Collections.unmodifiableList(words);
    }

    public static List<Pattern> getContainsPatterns() {
        return containsPatterns;
    }

    public static List<Pattern> getWordPatterns() {
        return wordPatterns;
    }

    /**
     * Builds a regex that matches a word even when characters are spaced or punctuated.
     */
    public static String buildSpacedWordRegex(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        StringBuilder regexBuilder = new StringBuilder();
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            regexBuilder.append(Pattern.quote(String.valueOf(chars[i])));
            if (i < chars.length - 1) {
                regexBuilder.append("\\s*[\\W_]*");
            }
        }
        return regexBuilder.toString();
    }

    public static String censor(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        StringBuilder rep = new StringBuilder(token.length());
        for (int i = 0; i < token.length(); i++) {
            rep.append('#');
        }
        return rep.toString();
    }
}
