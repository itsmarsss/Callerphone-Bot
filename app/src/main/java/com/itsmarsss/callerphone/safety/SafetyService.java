package com.itsmarsss.callerphone.safety;

import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.Match;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchMessage;
import com.itsmarsss.callerphone.match.model.MatchStatus;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchMessageRepository;
import com.itsmarsss.callerphone.match.repository.MatchRepository;
import com.itsmarsss.callerphone.match.service.MatchLimits;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class SafetyService {
    private final BlockRepository blocks;
    private final ReportRepository reports;
    private final SanctionRepository sanctions;
    private final AuditRepository audits;
    private final MatchRepository matches;
    private final MatchConversationRepository conversations;
    private MatchMessageRepository messages;
    private ReportAlerter reportAlerter;
    private BiConsumer<String, String> profilePauser;

    public SafetyService(
            BlockRepository blocks,
            ReportRepository reports,
            SanctionRepository sanctions,
            AuditRepository audits,
            MatchRepository matches,
            MatchConversationRepository conversations
    ) {
        this.blocks = blocks;
        this.reports = reports;
        this.sanctions = sanctions;
        this.audits = audits;
        this.matches = matches;
        this.conversations = conversations;
    }

    public void setMessages(MatchMessageRepository messages) {
        this.messages = messages;
    }

    public void setReportAlerter(ReportAlerter reportAlerter) {
        this.reportAlerter = reportAlerter;
    }

    public void setProfilePauser(BiConsumer<String, String> profilePauser) {
        this.profilePauser = profilePauser;
    }

    @FunctionalInterface
    public interface ReportAlerter {
        void onReport(Report report);
    }

    public boolean isBlockedEitherWay(String a, String b) {
        return blocks.existsEitherDirection(a, b, Block.PRODUCT_MATCH);
    }

    public boolean isMatchSuspended(String userId) {
        return sanctions.hasActiveSanction(userId, Block.PRODUCT_MATCH)
                || sanctions.hasActiveSanction(userId, Block.PRODUCT_GLOBAL);
    }

    public void block(String blockerId, String blockedId, String reason) {
        blocks.create(Block.match(blockerId, blockedId, reason));
        endPair(blockerId, blockedId, blockerId, MatchStatus.BLOCKED);
        audits.append(AuditEvent.of(blockerId, "block", blockedId, Block.PRODUCT_MATCH, reason));
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(blockerId, "safety_block", blockedId);
            }
        } catch (Exception ignored) {
        }
    }

    public Report report(
            String reporterId,
            String subjectId,
            String category,
            String details,
            String targetType,
            String targetId
    ) {
        ReportCategory cat = ReportCategory.from(category).orElse(ReportCategory.OTHER);
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setSubjectId(subjectId);
        report.setProduct(Block.PRODUCT_MATCH);
        report.setCategory(cat.code());
        report.setDetails(details == null ? "" : details);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        if (cat.urgent()) {
            report.setPriority(10);
        }
        report.setEvidence(collectEvidence(reporterId, subjectId, targetId));
        reports.save(report);
        audits.append(AuditEvent.of(reporterId, "report", subjectId, Block.PRODUCT_MATCH, cat.code()));

        // Urgent: auto-restrict visibility pending mod review (not a ban)
        if (cat.urgent() && profilePauser != null) {
            profilePauser.accept(subjectId, "Auto-paused pending review: " + cat.label());
            report.setAutoPaused(true);
            reports.save(report);
        }

        if (reportAlerter != null) {
            reportAlerter.onReport(report);
        }
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(reporterId, "safety_report", cat.code());
            }
        } catch (Exception ignored) {
        }
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox().push(
                        reporterId,
                        com.itsmarsss.callerphone.match.service.SocialInboxService.EntryType.SAFETY_UPDATE,
                        report.getId() != null ? report.getId() : subjectId,
                        "Safety",
                        "Report received · " + cat.label()
                );
            }
        } catch (Exception ignored) {
        }
        return report;
    }

    private List<String> collectEvidence(String reporterId, String subjectId, String targetId) {
        List<String> evidence = new ArrayList<>();
        if (messages == null) {
            return evidence;
        }
        // Prefer conversation if target is a conversation id
        if (targetId != null && !targetId.isBlank()) {
            conversations.findById(targetId).ifPresent(c -> appendMessages(evidence, c.getConversationId()));
        }
        if (evidence.isEmpty()) {
            for (MatchConversation c : conversations.findActiveByUserId(reporterId)) {
                if (subjectId != null && subjectId.equals(c.otherParticipant(reporterId))) {
                    appendMessages(evidence, c.getConversationId());
                    break;
                }
            }
        }
        return evidence;
    }

    private void appendMessages(List<String> evidence, String conversationId) {
        for (MatchMessage m : messages.findByConversation(conversationId, MatchLimits.REPORT_EVIDENCE_MESSAGES)) {
            evidence.add(m.createdAt() + " " + m.senderId() + ": " + m.content());
        }
    }

    public void suspendMatch(String moderatorId, String userId, String reason) {
        Sanction sanction = new Sanction();
        sanction.setUserId(userId);
        sanction.setProduct(Block.PRODUCT_MATCH);
        sanction.setType("suspend");
        sanction.setReason(reason);
        sanction.setModeratorId(moderatorId);
        sanctions.save(sanction);
        audits.append(AuditEvent.of(moderatorId, "suspend_match", userId, Block.PRODUCT_MATCH, reason));
    }

    public void restoreMatch(String moderatorId, String userId, String reason) {
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(moderatorId, "staff_restore", userId);
            }
        } catch (Exception ignored) {
        }
        for (Sanction sanction : sanctions.findActive(userId)) {
            if (Block.PRODUCT_MATCH.equals(sanction.getProduct()) || Block.PRODUCT_GLOBAL.equals(sanction.getProduct())) {
                sanction.setActive(false);
                sanctions.save(sanction);
            }
        }
        audits.append(AuditEvent.of(moderatorId, "restore_match", userId, Block.PRODUCT_MATCH, reason));
    }

    public List<Report> openReports(int limit) {
        return reports.findOpen(limit);
    }

    public void resolveReport(String moderatorId, String reportId, String status) {
        reports.findById(reportId).ifPresent(report -> {
            report.setStatus(status);
            report.setAssigneeId(moderatorId);
            reports.save(report);
            audits.append(AuditEvent.of(moderatorId, "resolve_report", reportId, Block.PRODUCT_MATCH, status));
        });
    }

    private void endPair(String a, String b, String endedBy, MatchStatus status) {
        String pairKey = Match.pairKeyFor(a, b);
        matches.findByPairKey(pairKey).ifPresent(match -> {
            match.setStatus(status);
            match.setEndedAt(Instant.now());
            match.setEndedBy(endedBy);
            matches.save(match);
            conversations.findByMatchId(match.getMatchId()).ifPresent(this::archiveConversation);
        });
    }

    private void archiveConversation(MatchConversation conversation) {
        conversation.setStage(ConversationStage.ARCHIVED);
        conversation.setArchivedAt(Instant.now());
        conversations.save(conversation);
    }
}
