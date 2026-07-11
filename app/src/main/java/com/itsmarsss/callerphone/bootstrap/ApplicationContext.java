package com.itsmarsss.callerphone.bootstrap;

import com.itsmarsss.callerphone.analytics.AnalyticsService;
import com.itsmarsss.callerphone.analytics.MongoProductEventRepository;
import com.itsmarsss.callerphone.identity.DeletionService;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.identity.MongoConsentRepository;
import com.itsmarsss.callerphone.identity.MongoMatchUserRepository;
import com.itsmarsss.callerphone.match.repository.MongoMatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MongoMatchDecisionRepository;
import com.itsmarsss.callerphone.match.repository.MongoMatchMessageRepository;
import com.itsmarsss.callerphone.match.repository.MongoMatchProfileRepository;
import com.itsmarsss.callerphone.match.repository.MongoMatchRepository;
import com.itsmarsss.callerphone.match.service.BrowseSessionStore;
import com.itsmarsss.callerphone.match.service.ConnectService;
import com.itsmarsss.callerphone.match.service.DecisionService;
import com.itsmarsss.callerphone.match.service.DiscoveryService;
import com.itsmarsss.callerphone.match.service.ExportService;
import com.itsmarsss.callerphone.match.service.MatchConversationService;
import com.itsmarsss.callerphone.match.service.MatchMaintenanceJobs;
import com.itsmarsss.callerphone.match.service.NotificationService;
import com.itsmarsss.callerphone.match.service.PremiumService;
import com.itsmarsss.callerphone.match.service.ProfileService;
import com.itsmarsss.callerphone.media.DiscordChannelMediaStorage;
import com.itsmarsss.callerphone.media.MediaStorage;
import com.itsmarsss.callerphone.persistence.IndexBootstrap;
import com.itsmarsss.callerphone.persistence.MigrationRunner;
import com.itsmarsss.callerphone.safety.MongoAuditRepository;
import com.itsmarsss.callerphone.safety.MongoBlockRepository;
import com.itsmarsss.callerphone.safety.MongoReportRepository;
import com.itsmarsss.callerphone.safety.MongoSanctionRepository;
import com.itsmarsss.callerphone.safety.SafetyService;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Constructs Match/Social services once. Does not rewrite legacy static registries.
 */
public final class ApplicationContext {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
    private static volatile ApplicationContext instance;

    private final DbExecutor dbExecutor;
    private final EnrollmentService enrollmentService;
    private final ProfileService profileService;
    private final DiscoveryService discoveryService;
    private final DecisionService decisionService;
    private final MatchConversationService conversationService;
    private final ConnectService connectService;
    private final SafetyService safetyService;
    private final PremiumService premiumService;
    private final NotificationService notificationService;
    private final DeletionService deletionService;
    private final AnalyticsService analyticsService;
    private final ExportService exportService;
    private final MatchMaintenanceJobs maintenanceJobs;
    private final MediaStorage mediaStorage;
    private final BrowseSessionStore browseSessionStore;
    private final com.itsmarsss.callerphone.match.repository.MatchDecisionRepository decisionRepository;

    private ApplicationContext(MongoDatabase database, String mediaChannelId, JDA jdaOrNull) {
        this.dbExecutor = new DbExecutor(4);

        var matchUsers = new MongoMatchUserRepository(database);
        var consents = new MongoConsentRepository(database);
        var profiles = new MongoMatchProfileRepository(database);
        var decisions = new MongoMatchDecisionRepository(database);
        var matches = new MongoMatchRepository(database);
        var conversations = new MongoMatchConversationRepository(database);
        var messages = new MongoMatchMessageRepository(database);
        var blocks = new MongoBlockRepository(database);
        var reports = new MongoReportRepository(database);
        var sanctions = new MongoSanctionRepository(database);
        var audits = new MongoAuditRepository(database);
        this.browseSessionStore = new BrowseSessionStore(database);
        this.premiumService = new PremiumService();
        this.decisionRepository = decisions;

        this.safetyService = new SafetyService(blocks, reports, sanctions, audits, matches, conversations);
        this.enrollmentService = new EnrollmentService(matchUsers, consents, profiles, safetyService);
        this.profileService = new ProfileService(profiles, matchUsers, enrollmentService, safetyService);
        this.notificationService = new NotificationService(matchUsers, profiles);
        this.analyticsService = new AnalyticsService(new MongoProductEventRepository(database));
        this.profileService.setNotifications(notificationService);
        this.profileService.setAnalytics(analyticsService);
        this.safetyService.setMessages(messages);
        this.safetyService.setReportAlerter(notificationService::postReportToStaff);
        this.safetyService.setProfilePauser((userId, reason) ->
                profileService.forcePause("system", userId, reason));

        this.discoveryService = new DiscoveryService(
                profiles, decisions, matches, matchUsers, safetyService, profileService, premiumService, browseSessionStore
        );
        this.decisionService = new DecisionService(
                decisions, matches, conversations, discoveryService, profileService, premiumService,
                safetyService, notificationService, analyticsService, matchUsers
        );
        this.conversationService = new MatchConversationService(
                conversations, messages, matches, matchUsers, profileService, safetyService
        );
        this.conversationService.setNotifications(notificationService);
        this.connectService = new ConnectService(conversations, profileService, consents, safetyService, notificationService);
        this.deletionService = new DeletionService(matchUsers, consents, profiles, matches, conversations, audits);
        this.exportService = new ExportService(matchUsers, profiles, conversations, messages);
        this.maintenanceJobs = new MatchMaintenanceJobs(database);
        this.maintenanceJobs.wire(matchUsers, conversations, profiles, notificationService);
        this.maintenanceJobs.start();
        this.mediaStorage = jdaOrNull == null
                ? new MediaStorage() {
            @Override
            public java.util.concurrent.CompletableFuture<com.itsmarsss.callerphone.match.model.MediaRef> storeFromUrl(
                    String ownerUserId, String sourceUrl, String contentType, int sortOrder) {
                return java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException("JDA not ready"));
            }

            @Override
            public java.util.Optional<String> resolveUrl(com.itsmarsss.callerphone.match.model.MediaRef ref) {
                return ref == null || ref.attachmentUrl() == null
                        ? java.util.Optional.empty()
                        : java.util.Optional.of(ref.attachmentUrl());
            }
        }
                : new DiscordChannelMediaStorage(jdaOrNull, mediaChannelId);

        new IndexBootstrap(database).bootstrap();
        new MigrationRunner(database).run(List.of(
                new MigrationRunner.Migration("match-001-init", "Match collections initialized", () -> {
                })
        ));
        logger.info("ApplicationContext ready (Match module)");
    }

    public static ApplicationContext init(MongoDatabase database, String mediaChannelId) {
        ApplicationContext ctx = new ApplicationContext(database, mediaChannelId, null);
        instance = ctx;
        return ctx;
    }

    public static ApplicationContext get() {
        ApplicationContext ctx = instance;
        if (ctx == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return ctx;
    }

    public static boolean isReady() {
        return instance != null;
    }

    public void attachJda(ShardManager shardManager, String mediaChannelId) {
        logger.info("JDA attached for Match media channel={}", mediaChannelId);
    }

    public void shutdown() {
        maintenanceJobs.shutdown();
        dbExecutor.shutdown();
    }

    public DbExecutor dbExecutor() {
        return dbExecutor;
    }

    public EnrollmentService enrollment() {
        return enrollmentService;
    }

    public ProfileService profiles() {
        return profileService;
    }

    public DiscoveryService discovery() {
        return discoveryService;
    }

    public DecisionService decisions() {
        return decisionService;
    }

    public MatchConversationService conversations() {
        return conversationService;
    }

    public ConnectService connect() {
        return connectService;
    }

    public SafetyService safety() {
        return safetyService;
    }

    public PremiumService premium() {
        return premiumService;
    }

    public NotificationService notifications() {
        return notificationService;
    }

    public DeletionService deletion() {
        return deletionService;
    }

    public AnalyticsService analytics() {
        return analyticsService;
    }

    public ExportService export() {
        return exportService;
    }

    public MediaStorage media() {
        return mediaStorage;
    }

    public java.util.List<com.itsmarsss.callerphone.match.model.MatchDecision> incomingLikes(String userId) {
        return decisionRepository.findIncomingInterested(userId, 20);
    }
}



