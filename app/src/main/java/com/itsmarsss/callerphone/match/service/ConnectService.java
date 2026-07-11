package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.AgeCohort;
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
            return EnrollmentService.ServiceResult.fail("Conversation not found.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() == ConversationStage.CONNECT_PENDING) {
            return EnrollmentService.ServiceResult.fail("A connect request is already pending.");
        }
        if (conversation.getStage() != ConversationStage.MEDIATED) {
            return EnrollmentService.ServiceResult.fail("Connect is not available for this conversation.");
        }
        String other = conversation.otherParticipant(userId);
        if (other == null || safety.isBlockedEitherWay(userId, other)) {
            return EnrollmentService.ServiceResult.fail("Cannot send connect request.");
        }
        Optional<MatchProfile> self = profiles.find(userId);
        Optional<MatchProfile> peer = profiles.find(other);
        if (self.isEmpty() || peer.isEmpty()) {
            return EnrollmentService.ServiceResult.fail("Profiles missing.");
        }
        if (self.get().getAgeCohort() != AgeCohort.AGE_18_PLUS
                || peer.get().getAgeCohort() != AgeCohort.AGE_18_PLUS) {
            return EnrollmentService.ServiceResult.fail(
                    "Identity connect is only available when both profiles are in the 18+ age group. Stay in mediated chat.");
        }
        conversation.setStage(ConversationStage.CONNECT_PENDING);
        conversation.setConnectRequestedBy(userId);
        conversation.setConnectRequestedAt(Instant.now());
        conversation.setConnectExpiresAt(Instant.now().plus(Duration.ofHours(MatchLimits.CONNECT_EXPIRE_HOURS)));
        conversations.save(conversation);
        notifications.notifyConnectRequest(other, userId, conversationId);
        return EnrollmentService.ServiceResult.ok("Connect request sent. They have 48 hours to accept.");
    }

    public ConnectResult accept(String userId, String conversationId) {
        Optional<MatchConversation> opt = conversations.findById(conversationId);
        if (opt.isEmpty() || !opt.get().getParticipants().contains(userId)) {
            return ConnectResult.fail("Conversation not found.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() != ConversationStage.CONNECT_PENDING) {
            return ConnectResult.fail("No pending connect request.");
        }
        if (userId.equals(conversation.getConnectRequestedBy())) {
            return ConnectResult.fail("You cannot accept your own request.");
        }
        if (conversation.getConnectExpiresAt() != null && conversation.getConnectExpiresAt().isBefore(Instant.now())) {
            conversation.setStage(ConversationStage.MEDIATED);
            conversation.setConnectRequestedBy(null);
            conversation.setConnectRequestedAt(null);
            conversation.setConnectExpiresAt(null);
            conversations.save(conversation);
            return ConnectResult.fail("Connect request expired.");
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
            return EnrollmentService.ServiceResult.fail("Conversation not found.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() != ConversationStage.CONNECT_PENDING) {
            return EnrollmentService.ServiceResult.fail("No pending request.");
        }
        if (userId.equals(conversation.getConnectRequestedBy())) {
            return EnrollmentService.ServiceResult.fail("Cancel is not available; wait for them to respond or unmatch.");
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
        return EnrollmentService.ServiceResult.ok("Connect request declined.");
    }

    public record ConnectResult(boolean success, String message, String otherUserId) {
        public static ConnectResult fail(String message) {
            return new ConnectResult(false, message, null);
        }

        public static ConnectResult connected(String otherUserId) {
            return new ConnectResult(true, "Connected. You may share Discord profiles with mutual consent.", otherUserId);
        }
    }
}
