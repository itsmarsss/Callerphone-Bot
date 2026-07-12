package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.identity.MatchUserRepository;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchMessage;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.MatchStatus;
import com.itsmarsss.callerphone.match.model.Match;
import com.itsmarsss.callerphone.match.repository.MatchConversationRepository;
import com.itsmarsss.callerphone.match.repository.MatchMessageRepository;
import com.itsmarsss.callerphone.match.repository.MatchRepository;
import com.itsmarsss.callerphone.match.validation.ProfileValidator;
import com.itsmarsss.callerphone.safety.SafetyService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class MatchConversationService {
    private final MatchConversationRepository conversations;
    private final MatchMessageRepository messages;
    private final MatchRepository matches;
    private final MatchUserRepository users;
    private final ProfileService profiles;
    private final SafetyService safety;
    private NotificationService notifications;

    public MatchConversationService(
            MatchConversationRepository conversations,
            MatchMessageRepository messages,
            MatchRepository matches,
            MatchUserRepository users,
            ProfileService profiles,
            SafetyService safety
    ) {
        this.conversations = conversations;
        this.messages = messages;
        this.matches = matches;
        this.users = users;
        this.profiles = profiles;
        this.safety = safety;
    }

    public void setNotifications(NotificationService notifications) {
        this.notifications = notifications;
    }

    public List<MatchConversation> list(String userId) {
        return conversations.findActiveByUserId(userId);
    }

    public Optional<MatchConversation> find(String conversationId) {
        return conversations.findById(conversationId);
    }

    public SelectResult select(String userId, String conversationId) {
        Optional<MatchConversation> opt = conversations.findById(conversationId);
        if (opt.isEmpty() || opt.get().getParticipants() == null || !opt.get().getParticipants().contains(userId)) {
            return SelectResult.fail("Chat not found.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() == ConversationStage.ARCHIVED) {
            return SelectResult.fail("That chat is closed.");
        }
        // expire stale connect requests on access
        if (conversation.getStage() == ConversationStage.CONNECT_PENDING
                && conversation.getConnectExpiresAt() != null
                && conversation.getConnectExpiresAt().isBefore(Instant.now())) {
            conversation.setStage(ConversationStage.MEDIATED);
            conversation.setConnectRequestedBy(null);
            conversation.setConnectRequestedAt(null);
            conversation.setConnectExpiresAt(null);
            conversations.save(conversation);
        }
        String other = conversation.otherParticipant(userId);
        if (other != null && safety.isBlockedEitherWay(userId, other)) {
            return SelectResult.fail("You can't chat with them.");
        }
        MatchUser user = users.findById(userId).orElseGet(() -> new MatchUser(userId));
        user.setSelectedConversationId(conversationId);
        user.setConversationSelectedAt(Instant.now());
        user.touch();
        users.save(user);
        conversation.clearUnread(userId);
        conversations.save(conversation);
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox()
                        .markReadBySource(userId, conversationId);
            }
        } catch (Exception ignored) {
        }
        MatchProfile self = profiles.find(userId).orElse(null);
        MatchProfile peer = profiles.find(other).orElse(null);
        String name = peer != null && peer.getDisplayName() != null && !peer.getDisplayName().isBlank()
                ? peer.getDisplayName()
                : "your match";
        String icebreaker = Icebreakers.forPair(self, peer);
        String message = "Chatting with **" + name + "**.\n"
                + "Send a text DM here to talk.\n\n"
                + "_" + icebreaker + "_";
        return SelectResult.ok(message, conversation, other, icebreaker);
    }

    public EnrollmentService.ServiceResult stopChat(String userId) {
        MatchUser user = users.findById(userId).orElse(null);
        if (user == null) {
            return EnrollmentService.ServiceResult.fail("Join Match first.");
        }
        user.setSelectedConversationId(null);
        user.setConversationSelectedAt(null);
        users.save(user);
        return EnrollmentService.ServiceResult.ok("Chat closed.");
    }

    public RelayResult relayDm(String senderId, String content, String sourceMessageId) {
        if (content == null || content.isBlank()) {
            return RelayResult.fail("Empty message.");
        }
        if (ProfileValidator.containsContactOrUrl(content)) {
            return RelayResult.fail("Messages cannot include contact info or links during mediated chat.");
        }
        MatchUser user = users.findById(senderId).orElse(null);
        if (user == null || user.getSelectedConversationId() == null) {
            return RelayResult.none();
        }
        if (user.getConversationSelectedAt() != null
                && user.getConversationSelectedAt().isBefore(Instant.now().minus(Duration.ofMinutes(MatchLimits.CHAT_IDLE_MINUTES)))) {
            user.setSelectedConversationId(null);
            user.setConversationSelectedAt(null);
            users.save(user);
            return RelayResult.fail("Chat context timed out. Select a conversation with `/match chats`.");
        }
        Optional<MatchConversation> opt = conversations.findById(user.getSelectedConversationId());
        if (opt.isEmpty()) {
            return RelayResult.fail("Selected conversation is gone.");
        }
        MatchConversation conversation = opt.get();
        if (conversation.getStage() == ConversationStage.ARCHIVED
                || conversation.getStage() == ConversationStage.CONNECTED) {
            // CONNECTED: still allow mediated? Plan says after connect may share identity; keep relay optional
            if (conversation.getStage() == ConversationStage.ARCHIVED) {
                return RelayResult.fail("Conversation archived.");
            }
        }
        String recipientId = conversation.otherParticipant(senderId);
        if (recipientId == null || safety.isBlockedEitherWay(senderId, recipientId)) {
            return RelayResult.fail("Cannot deliver this message.");
        }
        Optional<Match> match = matches.findById(conversation.getMatchId());
        if (match.isEmpty() || match.get().getStatus() != MatchStatus.ACTIVE) {
            return RelayResult.fail("Match is no longer active.");
        }

        String display = profiles.find(senderId).map(MatchProfile::getDisplayName).orElse("Match");
        messages.save(new MatchMessage(
                sourceMessageId,
                conversation.getConversationId(),
                senderId,
                recipientId,
                content,
                Instant.now(),
                "sent"
        ));
        conversation.setMessageCount(conversation.getMessageCount() + 1);
        conversation.setLastActivityAt(Instant.now());
        conversation.setLastMessagePreview(content.length() > 80 ? content.substring(0, 77) + "…" : content);
        conversation.incrementUnread(recipientId);
        conversation.clearUnread(senderId);
        String day = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString();
        String yesterday = java.time.LocalDate.now(java.time.ZoneOffset.UTC).minusDays(1).toString();
        if (!day.equals(conversation.getLastChatDay())) {
            if (yesterday.equals(conversation.getLastChatDay())) {
                conversation.setChatStreakDays(conversation.getChatStreakDays() + 1);
            } else {
                conversation.setChatStreakDays(1);
            }
            conversation.setLastChatDay(day);
        }
        conversations.save(conversation);
        user.setConversationSelectedAt(Instant.now());
        users.save(user);
        // Unified inbox is source of truth; DM is delivery (plan §10.F)
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                String preview = content.length() > 80 ? content.substring(0, 77) + "…" : content;
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox().push(
                        recipientId,
                        SocialInboxService.EntryType.CONNECTION_MESSAGE,
                        conversation.getConversationId(),
                        display,
                        preview
                );
            }
        } catch (Exception ignored) {
        }
        return RelayResult.relay(recipientId, display, content, conversation.getConversationId());
    }

    public EnrollmentService.ServiceResult unmatch(String userId, String conversationId) {
        Optional<MatchConversation> opt = conversations.findById(conversationId);
        if (opt.isEmpty() || !opt.get().getParticipants().contains(userId)) {
            return EnrollmentService.ServiceResult.fail("Chat not found.");
        }
        MatchConversation conversation = opt.get();
        matches.findById(conversation.getMatchId()).ifPresent(match -> {
            match.setStatus(MatchStatus.UNMATCHED);
            match.setEndedAt(Instant.now());
            match.setEndedBy(userId);
            matches.save(match);
        });
        conversation.setStage(ConversationStage.ARCHIVED);
        conversation.setArchivedAt(Instant.now());
        conversations.save(conversation);
        clearSelectionIf(userId, conversationId);
        String other = conversation.otherParticipant(userId);
        if (other != null) {
            clearSelectionIf(other, conversationId);
            if (notifications != null) {
                notifications.notifyUnmatched(other, userId, conversationId);
            }
        }
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                var inbox = com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox();
                inbox.clearSource(userId, conversationId);
                if (other != null) {
                    inbox.clearSource(other, conversationId);
                }
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(userId, "match_unmatch", conversationId);
            }
        } catch (Exception ignored) {
        }
        return EnrollmentService.ServiceResult.ok("Unmatched.");
    }

    private void clearSelectionIf(String userId, String conversationId) {
        users.findById(userId).ifPresent(user -> {
            if (conversationId.equals(user.getSelectedConversationId())) {
                user.setSelectedConversationId(null);
                user.setConversationSelectedAt(null);
                users.save(user);
            }
        });
    }

    public record SelectResult(
            boolean success,
            String message,
            MatchConversation conversation,
            String otherUserId,
            String icebreaker
    ) {
        public static SelectResult fail(String message) {
            return new SelectResult(false, message, null, null, null);
        }

        public static SelectResult ok(String message, MatchConversation conversation, String otherUserId, String icebreaker) {
            return new SelectResult(true, message, conversation, otherUserId, icebreaker);
        }
    }

    public record RelayResult(boolean handled, boolean success, String message, String recipientId,
                              String senderDisplay, String content, String conversationId) {
        public static RelayResult none() {
            return new RelayResult(false, false, null, null, null, null, null);
        }

        public static RelayResult fail(String message) {
            return new RelayResult(true, false, message, null, null, null, null);
        }

        public static RelayResult relay(String recipientId, String senderDisplay, String content, String conversationId) {
            return new RelayResult(true, true, "sent", recipientId, senderDisplay, content, conversationId);
        }
    }
}
