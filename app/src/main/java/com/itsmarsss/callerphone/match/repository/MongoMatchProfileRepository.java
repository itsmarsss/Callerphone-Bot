package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.match.model.Gender;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.MediaRef;
import com.itsmarsss.callerphone.match.model.ProfilePrompt;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.persistence.BsonTime;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MongoMatchProfileRepository implements MatchProfileRepository {
    private final MongoCollection<Document> collection;

    public MongoMatchProfileRepository(MongoDatabase database) {
        this.collection = database.getCollection(MatchCollections.MATCH_PROFILES);
    }

    @Override
    public Optional<MatchProfile> findByUserId(String userId) {
        Document doc = collection.find(Filters.eq("_id", userId)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    @Override
    public void save(MatchProfile profile) {
        collection.replaceOne(
                Filters.eq("_id", profile.getUserId()),
                toDocument(profile),
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public List<MatchProfile> findCandidates(CandidateQuery query) {
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq("state", ProfileState.ACTIVE.name()));
        filters.add(Filters.eq("ageCohort", query.ageCohort().code()));
        filters.add(Filters.ne("_id", query.viewerId()));
        if (query.excludeUserIds() != null && !query.excludeUserIds().isEmpty()) {
            filters.add(Filters.nin("_id", query.excludeUserIds()));
        }

        List<MatchProfile> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.and(filters))
                .sort(Sorts.descending("lastActiveAt"))
                .limit(Math.max(1, query.limit()))) {
            results.add(fromDocument(doc));
        }
        return results;
    }

    @Override
    public List<MatchProfile> findByState(ProfileState state, int limit) {
        List<MatchProfile> results = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("state", state.name()))
                .sort(Sorts.ascending("updatedAt"))
                .limit(limit)) {
            results.add(fromDocument(doc));
        }
        return results;
    }

    private static Document toDocument(MatchProfile profile) {
        List<Document> prompts = new ArrayList<>();
        for (ProfilePrompt prompt : profile.getPrompts()) {
            prompts.add(new Document("promptId", prompt.promptId()).append("answer", prompt.answer()));
        }
        List<Document> media = new ArrayList<>();
        for (MediaRef ref : profile.getMedia()) {
            media.add(new Document("mediaId", ref.mediaId())
                    .append("status", ref.status())
                    .append("sortOrder", ref.sortOrder())
                    .append("source", ref.source())
                    .append("channelId", ref.channelId())
                    .append("messageId", ref.messageId())
                    .append("attachmentUrl", ref.attachmentUrl())
                    .append("contentType", ref.contentType()));
        }
        List<String> openTo = new ArrayList<>();
        for (Gender g : profile.getOpenToMeeting()) {
            openTo.add(g.code());
        }
        return new Document("_id", profile.getUserId())
                .append("schemaVersion", profile.getSchemaVersion())
                .append("state", profile.getState().name())
                .append("displayName", profile.getDisplayName())
                .append("ageCohort", profile.getAgeCohort() == null ? null : profile.getAgeCohort().code())
                .append("gender", profile.getGender() == null ? null : profile.getGender().code())
                .append("pronouns", profile.getPronouns())
                .append("openToMeeting", openTo)
                .append("bio", profile.getBio())
                .append("interests", profile.getInterests())
                .append("prompts", prompts)
                .append("media", media)
                .append("createdAt", BsonTime.toDate(profile.getCreatedAt()))
                .append("updatedAt", BsonTime.toDate(profile.getUpdatedAt()))
                .append("lastActiveAt", BsonTime.toDate(profile.getLastActiveAt()))
                .append("onboardingStep", profile.getOnboardingStep())
                .append("discoveryViewsToday", profile.getDiscoveryViewsToday())
                .append("interestSignalsToday", profile.getInterestSignalsToday())
                .append("usageDay", profile.getUsageDay());
    }

    private static MatchProfile fromDocument(Document doc) {
        MatchProfile profile = new MatchProfile(doc.getString("_id"));
        profile.setSchemaVersion(doc.getInteger("schemaVersion", 1));
        profile.setState(ProfileState.from(doc.getString("state")).orElse(ProfileState.DRAFT));
        profile.setDisplayName(doc.getString("displayName"));
        profile.setAgeCohort(AgeCohort.fromCode(doc.getString("ageCohort")).orElse(null));
        profile.setGender(Gender.fromCode(doc.getString("gender")).orElse(null));
        profile.setPronouns(doc.getString("pronouns"));
        List<Gender> openTo = new ArrayList<>();
        List<String> openRaw = doc.getList("openToMeeting", String.class);
        if (openRaw != null) {
            for (String code : openRaw) {
                Gender.fromCode(code).ifPresent(openTo::add);
            }
        }
        profile.setOpenToMeeting(openTo);
        profile.setBio(doc.getString("bio"));
        List<String> interests = doc.getList("interests", String.class);
        profile.setInterests(interests == null ? List.of() : interests);
        List<ProfilePrompt> prompts = new ArrayList<>();
        List<Document> promptDocs = doc.getList("prompts", Document.class);
        if (promptDocs != null) {
            for (Document p : promptDocs) {
                prompts.add(new ProfilePrompt(p.getString("promptId"), p.getString("answer")));
            }
        }
        profile.setPrompts(prompts);
        List<MediaRef> media = new ArrayList<>();
        List<Document> mediaDocs = doc.getList("media", Document.class);
        if (mediaDocs != null) {
            for (Document m : mediaDocs) {
                media.add(new MediaRef(
                        m.getString("mediaId"),
                        m.getString("status"),
                        m.getInteger("sortOrder", 0),
                        m.getString("source"),
                        m.getString("channelId"),
                        m.getString("messageId"),
                        m.getString("attachmentUrl"),
                        m.getString("contentType")
                ));
            }
        }
        profile.setMedia(media);
        profile.setCreatedAt(BsonTime.toInstant(doc.get("createdAt")));
        profile.setUpdatedAt(BsonTime.toInstant(doc.get("updatedAt")));
        profile.setLastActiveAt(BsonTime.toInstant(doc.get("lastActiveAt")));
        profile.setOnboardingStep(doc.getInteger("onboardingStep", 0));
        Number views = (Number) doc.get("discoveryViewsToday");
        profile.setDiscoveryViewsToday(views == null ? 0 : views.longValue());
        Number interestsToday = (Number) doc.get("interestSignalsToday");
        profile.setInterestSignalsToday(interestsToday == null ? 0 : interestsToday.longValue());
        profile.setUsageDay(doc.getString("usageDay"));
        return profile;
    }
}
