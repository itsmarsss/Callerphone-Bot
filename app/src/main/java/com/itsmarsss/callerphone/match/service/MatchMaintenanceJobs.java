package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.persistence.MatchCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodic Match jobs: connect expiry, idle archive, message scrub, inactivity nudges, weekly digest.
 */
public final class MatchMaintenanceJobs {
    private static final Logger logger = LoggerFactory.getLogger(MatchMaintenanceJobs.class);
    private static final Duration IDLE_ARCHIVE = Duration.ofDays(14);
    private static final Duration MESSAGE_RETENTION = Duration.ofDays(30);

    private final MongoDatabase database;
    private final ScheduledExecutorService scheduler;
    private MatchUserRepository users;
    private MatchConversationRepository conversations;
    private MatchProfileRepository profiles;
    private NotificationService notifications;

    public MatchMaintenanceJobs(MongoDatabase database) {
        this.database = database;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "match-maintenance");
            t.setDaemon(true);
            return t;
        });
    }

    public void wire(
            MatchUserRepository users,
            MatchConversationRepository conversations,
            MatchProfileRepository profiles,
            NotificationService notifications
    ) {
        this.users = users;
        this.conversations = conversations;
        this.profiles = profiles;
        this.notifications = notifications;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::safeRun, 2, 15, TimeUnit.MINUTES);
        logger.info("Match maintenance jobs scheduled");
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void safeRun() {
        try {
            expireConnectRequests();
            archiveIdleConversations();
            scrubOldMessageContent();
            sendInactivityNudges();
            sendWeeklyDigests();
        } catch (Exception e) {
            logger.warn("Match maintenance tick failed: {}", e.getMessage());
        }
    }

    private void expireConnectRequests() {
        MongoCollection<Document> col = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
        Date now = Date.from(Instant.now());
        var result = col.updateMany(
                Filters.and(
                        Filters.eq("stage", ConversationStage.CONNECT_PENDING.name()),
                        Filters.lt("connectExpiresAt", now)
                ),
                Updates.combine(
                        Updates.set("stage", ConversationStage.MEDIATED.name()),
                        Updates.unset("connectRequestedBy"),
                        Updates.unset("connectRequestedAt"),
                        Updates.unset("connectExpiresAt")
                )
        );
        if (result.getModifiedCount() > 0) {
            logger.info("Expired {} connect requests", result.getModifiedCount());
        }
    }

    private void archiveIdleConversations() {
        MongoCollection<Document> col = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
        Date cutoff = Date.from(Instant.now().minus(IDLE_ARCHIVE));
        var result = col.updateMany(
                Filters.and(
                        Filters.ne("stage", ConversationStage.ARCHIVED.name()),
                        Filters.lt("lastActivityAt", cutoff)
                ),
                Updates.combine(
                        Updates.set("stage", ConversationStage.ARCHIVED.name()),
                        Updates.set("archivedAt", Date.from(Instant.now()))
                )
        );
        if (result.getModifiedCount() > 0) {
            logger.info("Archived {} idle Match conversations", result.getModifiedCount());
        }
    }

    private void scrubOldMessageContent() {
        MongoCollection<Document> col = database.getCollection(MatchCollections.MATCH_MESSAGES);
        Date cutoff = Date.from(Instant.now().minus(MESSAGE_RETENTION));
        MongoCollection<Document> convs = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
        for (Document conv : convs.find(Filters.and(
                Filters.eq("stage", ConversationStage.ARCHIVED.name()),
                Filters.lt("archivedAt", cutoff)
        )).limit(200)) {
            String conversationId = conv.getString("_id");
            col.updateMany(
                    Filters.and(
                            Filters.eq("conversationId", conversationId),
                            Filters.ne("content", "[redacted]")
                    ),
                    Updates.set("content", "[redacted]")
            );
        }
    }

    private void sendInactivityNudges() {
        if (conversations == null || profiles == null || notifications == null || users == null) {
            return;
        }
        Instant cutoff = Instant.now().minus(Duration.ofHours(MatchLimits.NUDGE_AFTER_HOURS));
        MongoCollection<Document> col = database.getCollection(MatchCollections.MATCH_CONVERSATIONS);
        for (Document doc : col.find(Filters.and(
                Filters.ne("stage", ConversationStage.ARCHIVED.name()),
                Filters.lt("lastActivityAt", Date.from(cutoff)),
                Filters.eq("lastNudgeAt", null)
        )).limit(30)) {
            String conversationId = doc.getString("_id");
            conversations.findById(conversationId).ifPresent(c -> {
                if (c.getLastNudgeAt() != null || c.getMessageCount() == 0) {
                    return;
                }
                List<String> parts = c.getParticipants();
                if (parts == null || parts.size() < 2) {
                    return;
                }
                for (String userId : parts) {
                    MatchUser user = users.findById(userId).orElse(null);
                    if (user == null || !user.isNotificationsEnabled() || !user.isEnrolled()) {
                        continue;
                    }
                    String other = c.otherParticipant(userId);
                    String name = profiles.findByUserId(other).map(MatchProfile::getDisplayName).orElse("your match");
                    String opener = Icebreakers.forPair(
                            profiles.findByUserId(userId).orElse(null),
                            profiles.findByUserId(other).orElse(null)
                    );
                    notifications.notifyInactivityNudge(userId, name, conversationId, opener);
                }
                c.setLastNudgeAt(Instant.now());
                conversations.save(c);
            });
        }
    }

    private void sendWeeklyDigests() {
        if (users == null || profiles == null || notifications == null || conversations == null) {
            return;
        }
        Instant weekAgo = Instant.now().minus(Duration.ofDays(7));
        for (MatchUser user : users.findDigestOptIn(40)) {
            if (user.getLastDigestAt() != null && user.getLastDigestAt().isAfter(weekAgo)) {
                continue;
            }
            long activeChats = conversations.countActiveByUserId(user.getUserId());
            MatchProfile profile = profiles.findByUserId(user.getUserId()).orElse(null);
            String state = profile == null ? "none" : String.valueOf(profile.getState());
            int completion = ProfileChecklist.completionPercent(user, profile);
            String body = "Weekly snapshot for your age group:\n"
                    + "• Profile: `" + state + "` (" + completion + "% complete)\n"
                    + "• Active chats: **" + activeChats + "**\n"
                    + "• Browse streak: **" + user.getBrowseStreakDays() + "** days\n\n"
                    + (profile != null && profile.getState() == ProfileState.ACTIVE
                    ? "Keep browsing with `/match browse`."
                    : "Finish and go live with `/match profile`.");
            notifications.notifyWeeklyDigest(user.getUserId(), body);
            user.setLastDigestAt(Instant.now());
            users.save(user);
        }
    }
}
