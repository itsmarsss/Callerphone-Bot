package com.itsmarsss.callerphone.match.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Match {
    private String matchId;
    private String pairKey;
    private List<String> userIds;
    private MatchStatus status = MatchStatus.ACTIVE;
    private Instant matchedAt = Instant.now();
    private Instant endedAt;
    private String endedBy;

    public Match() {
    }

    public static String pairKeyFor(String a, String b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return a.compareTo(b) <= 0 ? a + ":" + b : b + ":" + a;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getPairKey() {
        return pairKey;
    }

    public void setPairKey(String pairKey) {
        this.pairKey = pairKey;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Instant getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(Instant matchedAt) {
        this.matchedAt = matchedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getEndedBy() {
        return endedBy;
    }

    public void setEndedBy(String endedBy) {
        this.endedBy = endedBy;
    }

    public String otherUserId(String userId) {
        if (userIds == null) {
            return null;
        }
        for (String id : userIds) {
            if (!id.equals(userId)) {
                return id;
            }
        }
        return null;
    }
}
