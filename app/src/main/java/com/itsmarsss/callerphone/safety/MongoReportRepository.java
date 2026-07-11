package com.itsmarsss.callerphone.safety;

import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MongoReportRepository implements ReportRepository {
    private final MongoCollection<Document> collection;

    public MongoReportRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.REPORTS);
    }

    @Override
    public void save(Report report) {
        collection.replaceOne(
                Filters.eq("_id", report.getId()),
                toDocument(report),
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public Optional<Report> findById(String id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public List<Report> findOpen(int limit) {
        List<Report> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("status", "open"))
                .sort(Sorts.orderBy(Sorts.descending("priority"), Sorts.ascending("createdAt")))
                .limit(limit)) {
            results.add(fromDocument(doc));
        }
        return results;
    }

    private static Document toDocument(Report report) {
        return new Document("_id", report.getId())
                .append("reporterId", report.getReporterId())
                .append("subjectId", report.getSubjectId())
                .append("product", report.getProduct())
                .append("targetType", report.getTargetType())
                .append("targetId", report.getTargetId())
                .append("category", report.getCategory())
                .append("details", report.getDetails())
                .append("status", report.getStatus())
                .append("assigneeId", report.getAssigneeId())
                .append("priority", report.getPriority())
                .append("createdAt", BsonTime.toDate(report.getCreatedAt()));
    }

    private static Report fromDocument(Document doc) {
        Report report = new Report();
        report.setId(doc.getString("_id"));
        report.setReporterId(doc.getString("reporterId"));
        report.setSubjectId(doc.getString("subjectId"));
        report.setProduct(doc.getString("product"));
        report.setTargetType(doc.getString("targetType"));
        report.setTargetId(doc.getString("targetId"));
        report.setCategory(doc.getString("category"));
        report.setDetails(doc.getString("details"));
        report.setStatus(doc.getString("status"));
        report.setAssigneeId(doc.getString("assigneeId"));
        report.setPriority(doc.getInteger("priority", 1));
        report.setCreatedAt(BsonTime.toInstant(doc.get("createdAt")));
        return report;
    }
}
