package com.itsmarsss.callerphone.match.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MatchConversation {
    private String conversationId;
    private String matchId;
    private List<String> participants;
    private ConversationStage stage = ConversationStage.MEDIATED;
    private String connectRequestedBy;
    private Instant connectRequestedAt;
    private Instant connectExpiresAt;
    private long messageCount;
    private Instant createdAt = Instant.now();
    private Instant lastActivityAt = Instant.now();
    private Instant connectedAt;
    private Instant archivedAt;
    private Map<String, Integer> unreadByUser = new HashMap<>();
    private String lastMessagePreview = "";
    private Instant lastNudgeAt;
    private int chatStreakDays;
    private String lastChatDay = "";

    public MatchConversation() {
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public ConversationStage getStage() {
        return stage;
    }

    public void setStage(ConversationStage stage) {
        this.stage = stage;
    }

    public String getConnectRequestedBy() {
        return connectRequestedBy;
    }

    public void setConnectRequestedBy(String connectRequestedBy) {
        this.connectRequestedBy = connectRequestedBy;
    }

    public Instant getConnectRequestedAt() {
        return connectRequestedAt;
    }

    public void setConnectRequestedAt(Instant connectRequestedAt) {
        this.connectRequestedAt = connectRequestedAt;
    }

    public Instant getConnectExpiresAt() {
        return connectExpiresAt;
    }

    public void setConnectExpiresAt(Instant connectExpiresAt) {
        this.connectExpiresAt = connectExpiresAt;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    public Map<String, Integer> getUnreadByUser() {
        return unreadByUser;
    }

    public void setUnreadByUser(Map<String, Integer> unreadByUser) {
        this.unreadByUser = unreadByUser == null ? new HashMap<>() : new HashMap<>(unreadByUser);
    }

    public int unreadFor(String userId) {
        return unreadByUser.getOrDefault(userId, 0);
    }

    public void incrementUnread(String userId) {
        unreadByUser.merge(userId, 1, Integer::sum);
    }

    public void clearUnread(String userId) {
        unreadByUser.put(userId, 0);
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview == null ? "" : lastMessagePreview;
    }

    public Instant getLastNudgeAt() {
        return lastNudgeAt;
    }

    public void setLastNudgeAt(Instant lastNudgeAt) {
        this.lastNudgeAt = lastNudgeAt;
    }

    public int getChatStreakDays() {
        return chatStreakDays;
    }

    public void setChatStreakDays(int chatStreakDays) {
        this.chatStreakDays = chatStreakDays;
    }

    public String getLastChatDay() {
        return lastChatDay;
    }

    public void setLastChatDay(String lastChatDay) {
        this.lastChatDay = lastChatDay == null ? "" : lastChatDay;
    }

    public String otherParticipant(String userId) {
        if (participants == null) {
            return null;
        }
        for (String id : participants) {
            if (!id.equals(userId)) {
                return id;
            }
        }
        return null;
    }
}
