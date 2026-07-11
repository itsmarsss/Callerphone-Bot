package com.itsmarsss.callerphone.safety;

import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.Match;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchStatus;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchRepository;

import java.time.Instant;
import java.util.List;

public final class SafetyService {
    private final BlockRepository blocks;
    private final ReportRepository reports;
    private final SanctionRepository sanctions;
    private final AuditRepository audits;
    private final MatchRepository matches;
    private final MatchConversationRepository conversations;

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
    }

    public Report report(
            String reporterId,
            String subjectId,
            String category,
            String details,
            String targetType,
            String targetId
    ) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setSubjectId(subjectId);
        report.setProduct(Block.PRODUCT_MATCH);
        report.setCategory(category);
        report.setDetails(details);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        if (isUrgent(category)) {
            report.setPriority(10);
        }
        reports.save(report);
        audits.append(AuditEvent.of(reporterId, "report", subjectId, Block.PRODUCT_MATCH, category));
        return report;
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

    private static boolean isUrgent(String category) {
        if (category == null) {
            return false;
        }
        String c = category.toLowerCase();
        return c.contains("groom") || c.contains("minor") || c.contains("age")
                || c.contains("exploit") || c.contains("threat") || c.contains("contact");
    }
}
