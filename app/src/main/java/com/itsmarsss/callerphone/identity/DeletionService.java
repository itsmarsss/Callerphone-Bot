package com.itsmarsss.callerphone.identity;

import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.Match;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.MatchStatus;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.match.repository.MatchRepository;
import com.itsmarsss.callerphone.safety.AuditEvent;
import com.itsmarsss.callerphone.safety.AuditRepository;

import java.time.Instant;
import java.util.List;

/**
 * Soft-delete Match Social data without touching legacy users/credits collections.
 */
public final class DeletionService {
    private final MatchUserRepository users;
    private final ConsentRepository consents;
    private final MatchProfileRepository profiles;
    private final MatchRepository matches;
    private final MatchConversationRepository conversations;
    private final AuditRepository audits;

    public DeletionService(
            MatchUserRepository users,
            ConsentRepository consents,
            MatchProfileRepository profiles,
            MatchRepository matches,
            MatchConversationRepository conversations,
            AuditRepository audits
    ) {
        this.users = users;
        this.consents = consents;
        this.profiles = profiles;
        this.matches = matches;
        this.conversations = conversations;
        this.audits = audits;
    }

    public EnrollmentService.ServiceResult leaveAndSoftDelete(String userId) {
        MatchUser user = users.findById(userId).orElseGet(() -> new MatchUser(userId));
        user.setEnrolled(false);
        user.setLeftAt(Instant.now());
        user.setSelectedConversationId(null);
        user.setConversationSelectedAt(null);
        user.setNotificationsEnabled(false);
        user.touch();
        users.save(user);
        consents.append(ConsentEvent.of(userId, ConsentType.ENROLLMENT, "v1", false, "leave_soft_delete"));

        profiles.findByUserId(userId).ifPresent(profile -> {
            scrubProfile(profile);
            profiles.save(profile);
        });

        List<Match> active = matches.findActiveByUserId(userId);
        for (Match match : active) {
            match.setStatus(MatchStatus.ARCHIVED);
            match.setEndedAt(Instant.now());
            match.setEndedBy(userId);
            matches.save(match);
            conversations.findByMatchId(match.getMatchId()).ifPresent(c -> {
                c.setStage(ConversationStage.ARCHIVED);
                c.setArchivedAt(Instant.now());
                conversations.save(c);
            });
        }
        // also archive any lingering non-archived conversations
        for (MatchConversation conversation : conversations.findActiveByUserId(userId)) {
            conversation.setStage(ConversationStage.ARCHIVED);
            conversation.setArchivedAt(Instant.now());
            conversations.save(conversation);
        }

        audits.append(AuditEvent.of(userId, "match_leave_soft_delete", userId, "match", "user left social"));
        return EnrollmentService.ServiceResult.ok(
                "You left Match. Profile content was cleared, chats archived, and discovery stopped. "
                        + "Legacy bot account data (credits, etc.) is unchanged. Use `/match join` to start over.");
    }

    private static void scrubProfile(MatchProfile profile) {
        profile.setState(ProfileState.DELETED);
        profile.setDisplayName("deleted");
        profile.setBio("");
        profile.setPronouns("");
        profile.setInterests(List.of());
        profile.setPrompts(List.of());
        profile.setMedia(List.of());
        profile.setOpenToMeeting(List.of());
        profile.setGender(null);
        profile.touch();
    }
}
