package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.ConsentEvent;
import com.itsmarsss.callerphone.identity.ConsentRepository;
import com.itsmarsss.callerphone.identity.ConsentType;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.safety.SafetyService;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public final class ConnectService {
    private final MatchConversationRepository conversations;
    private final ProfileService profiles;
    private final ConsentRepository consents;
    private final SafetyService safety;
    private final NotificationService notifications;

    public ConnectService(
            MatchConversationRepository conversations,
            ProfileService profiles,
            ConsentRepository consents,
            SafetyService safety,
            NotificationService notifications
    ) {
        this.conversations = conversations;
        this.profiles = profiles;
        this.consents = consents;
        this.safety = safety;
        this.notifications = notifications;
    }

    public EnrollmentService.ServiceResult request(String userId, String conversationId) {
        Optional<MatchConversation> opt = conversations.findById(conversationId);
        if (opt.isEmpty() || !opt.get().getParticipants().contains(userId)) {
            return EnrollmentService.ServiceResult.fail("Chat not found.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() == ConversationStage.CONNECT_PENDING) {
            return EnrollmentService.ServiceResult.fail("A request is already pending.");
        }
        if (conversation.getStage() != ConversationStage.MEDIATED) {
            return EnrollmentService.ServiceResult.fail("Can't connect on this chat.");
        }
        String other = conversation.otherParticipant(userId);
        if (other == null || safety.isBlockedEitherWay(userId, other)) {
            return EnrollmentService.ServiceResult.fail("Can't send that request.");
        }
        Optional<MatchProfile> self = profiles.find(userId);
        Optional<MatchProfile> peer = profiles.find(other);
        if (self.isEmpty() || peer.isEmpty()) {
            return EnrollmentService.ServiceResult.fail("Profiles missing.");
        }
        // Age group is pairing isolation only — connect is available for every cohort the same way
        conversation.setStage(ConversationStage.CONNECT_PENDING);
        conversation.setConnectRequestedBy(userId);
        conversation.setConnectRequestedAt(Instant.now());
        conversation.setConnectExpiresAt(Instant.now().plus(Duration.ofHours(MatchLimits.CONNECT_EXPIRE_HOURS)));
        conversations.save(conversation);
        notifications.notifyConnectRequest(other, userId, conversationId);
        return EnrollmentService.ServiceResult.ok("Request sent. They have 48 hours.");
    }

    public ConnectResult accept(String userId, String conversationId) {
        Optional<MatchConversation> opt = conversations.findById(conversationId);
        if (opt.isEmpty() || !opt.get().getParticipants().contains(userId)) {
            return ConnectResult.fail("Chat not found.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() != ConversationStage.CONNECT_PENDING) {
            return ConnectResult.fail("No pending request.");
        }
        if (userId.equals(conversation.getConnectRequestedBy())) {
            return ConnectResult.fail("You can't accept your own request.");
        }
        if (conversation.getConnectExpiresAt() != null && conversation.getConnectExpiresAt().isBefore(Instant.now())) {
            conversation.setStage(ConversationStage.MEDIATED);
            conversation.setConnectRequestedBy(null);
            conversation.setConnectRequestedAt(null);
            conversation.setConnectExpiresAt(null);
            conversations.save(conversation);
            return ConnectResult.fail("That request expired.");
        }
        String other = conversation.otherParticipant(userId);
        conversation.setStage(ConversationStage.CONNECTED);
        conversation.setConnectedAt(Instant.now());
        conversations.save(conversation);
        consents.append(ConsentEvent.of(userId, ConsentType.CONNECTION, "v1", true, conversationId));
        if (other != null) {
            consents.append(ConsentEvent.of(other, ConsentType.CONNECTION, "v1", true, conversationId));
            notifications.notifyConnectAccepted(other, userId);
        }
        return ConnectResult.connected(other);
    }

    public EnrollmentService.ServiceResult decline(String userId, String conversationId) {
        Optional<MatchConversation> opt = conversations.findById(conversationId);
        if (opt.isEmpty() || !opt.get().getParticipants().contains(userId)) {
            return EnrollmentService.ServiceResult.fail("Chat not found.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() != ConversationStage.CONNECT_PENDING) {
            return EnrollmentService.ServiceResult.fail("No pending request.");
        }
        if (userId.equals(conversation.getConnectRequestedBy())) {
            return EnrollmentService.ServiceResult.fail("Wait for them to respond.");
        }
        String requester = conversation.getConnectRequestedBy();
        conversation.setStage(ConversationStage.MEDIATED);
        conversation.setConnectRequestedBy(null);
        conversation.setConnectRequestedAt(null);
        conversation.setConnectExpiresAt(null);
        conversations.save(conversation);
        if (requester != null) {
            notifications.notifyConnectDeclined(requester);
        }
        return EnrollmentService.ServiceResult.ok("Declined.");
    }

    public record ConnectResult(boolean success, String message, String otherUserId) {
        public static ConnectResult fail(String message) {
            return new ConnectResult(false, message, null);
        }

        public static ConnectResult connected(String otherUserId) {
            return new ConnectResult(true, "You're connected.", otherUserId);
        }
    }
}
