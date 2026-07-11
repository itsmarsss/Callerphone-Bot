package com.itsmarsss.callerphone.identity;

import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchProfileRepository;
import com.itsmarsss.callerphone.match.repository.MatchRepository;
import com.itsmarsss.callerphone.safety.AuditEvent;
import com.itsmarsss.callerphone.safety.AuditRepository;

import java.time.Instant;

/**
 * Retention-friendly leave: stop discovery, keep profile + connections so return is easy.
 * Does not touch legacy users/credits collections.
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

    /**
     * Leave Social discovery without wiping data.
     * Profile is paused (kept), chats remain, rejoin restores the same card.
     */
    public EnrollmentService.ServiceResult leaveAndSoftDelete(String userId) {
        MatchUser user = users.findById(userId).orElseGet(() -> new MatchUser(userId));
        user.setEnrolled(false);
        user.setLeftAt(Instant.now());
        user.setSelectedConversationId(null);
        user.setConversationSelectedAt(null);
        // keep notificationsEnabled so mutual-match style pings can still land if they rejoin soon
        user.touch();
        users.save(user);
        consents.append(ConsentEvent.of(userId, ConsentType.ENROLLMENT, "v1", false, "leave_pause"));

        profiles.findByUserId(userId).ifPresent(profile -> {
            if (profile.getState() == ProfileState.ACTIVE) {
                profile.setState(ProfileState.PAUSED);
            }
            profile.touch();
            profiles.save(profile);
        });

        // Keep matches/conversations — retention depends on unfinished chats still being there
        audits.append(AuditEvent.of(userId, "match_leave_pause", userId, "match", "user left discovery"));
        return EnrollmentService.ServiceResult.ok(
                "You left discovery. Your profile is paused but **not deleted**, and existing chats stay open. "
                        + "Come back anytime with `/match join` then `/match resume` — no rebuild needed.");
    }

    /** Hard wipe for explicit GDPR-style delete (staff or future `/match delete`). */
    public EnrollmentService.ServiceResult hardDeleteProfileContent(String userId) {
        profiles.findByUserId(userId).ifPresent(profile -> {
            scrubProfile(profile);
            profiles.save(profile);
        });
        audits.append(AuditEvent.of(userId, "match_hard_scrub", userId, "match", "profile content scrubbed"));
        return EnrollmentService.ServiceResult.ok("Match profile content scrubbed for " + userId);
    }

    private static void scrubProfile(MatchProfile profile) {
        profile.setState(ProfileState.DELETED);
        profile.setDisplayName("deleted");
        profile.setBio("");
        profile.setPronouns("");
        profile.setInterests(java.util.List.of());
        profile.setPrompts(java.util.List.of());
        profile.setMedia(java.util.List.of());
        profile.setOpenToMeeting(java.util.List.of());
        profile.setGender(null);
        profile.touch();
    }
}
