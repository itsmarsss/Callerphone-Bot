package com.itsmarsss.callerphone.match.model;

import java.time.Instant;
import java.util.List;

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
