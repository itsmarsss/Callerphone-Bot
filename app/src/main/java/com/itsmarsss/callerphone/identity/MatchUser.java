package com.itsmarsss.callerphone.identity;

import java.time.Instant;

/**
 * Match-side identity document. Kept separate from legacy users collection
 * so existing credits/status documents are never rewritten by Social.
 */
public final class MatchUser {
    private String userId;
    private int schemaVersion = 1;
    private boolean enrolled;
    private AgeCohort ageCohort;
    private Instant ageSelectedAt;
    private boolean notificationsEnabled = true;
    private Instant createdAt = Instant.now();
    private Instant lastActiveAt = Instant.now();
    private String termsVersionAccepted = "";
    private String privacyVersionAccepted = "";
    private Instant enrolledAt;
    private Instant leftAt;
    private String selectedConversationId;
    private Instant conversationSelectedAt;
    private String lastSkipSubjectId;
    private Instant lastSkipAt;
    private int undosToday;
    private String usageDay = "";
    private boolean digestOptIn;
    private Instant lastDigestAt;
    private int browseStreakDays;
    private String lastBrowseDay = "";

    public MatchUser() {
    }

    public MatchUser(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }

    public AgeCohort getAgeCohort() {
        return ageCohort;
    }

    public void setAgeCohort(AgeCohort ageCohort) {
        this.ageCohort = ageCohort;
    }

    public Instant getAgeSelectedAt() {
        return ageSelectedAt;
    }

    public void setAgeSelectedAt(Instant ageSelectedAt) {
        this.ageSelectedAt = ageSelectedAt;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public String getTermsVersionAccepted() {
        return termsVersionAccepted;
    }

    public void setTermsVersionAccepted(String termsVersionAccepted) {
        this.termsVersionAccepted = termsVersionAccepted == null ? "" : termsVersionAccepted;
    }

    public String getPrivacyVersionAccepted() {
        return privacyVersionAccepted;
    }

    public void setPrivacyVersionAccepted(String privacyVersionAccepted) {
        this.privacyVersionAccepted = privacyVersionAccepted == null ? "" : privacyVersionAccepted;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Instant enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public String getSelectedConversationId() {
        return selectedConversationId;
    }

    public void setSelectedConversationId(String selectedConversationId) {
        this.selectedConversationId = selectedConversationId;
    }

    public Instant getConversationSelectedAt() {
        return conversationSelectedAt;
    }

    public void setConversationSelectedAt(Instant conversationSelectedAt) {
        this.conversationSelectedAt = conversationSelectedAt;
    }

    public String getLastSkipSubjectId() {
        return lastSkipSubjectId;
    }

    public void setLastSkipSubjectId(String lastSkipSubjectId) {
        this.lastSkipSubjectId = lastSkipSubjectId;
    }

    public Instant getLastSkipAt() {
        return lastSkipAt;
    }

    public void setLastSkipAt(Instant lastSkipAt) {
        this.lastSkipAt = lastSkipAt;
    }

    public int getUndosToday() {
        return undosToday;
    }

    public void setUndosToday(int undosToday) {
        this.undosToday = undosToday;
    }

    public String getUsageDay() {
        return usageDay;
    }

    public void setUsageDay(String usageDay) {
        this.usageDay = usageDay == null ? "" : usageDay;
    }

    public boolean isDigestOptIn() {
        return digestOptIn;
    }

    public void setDigestOptIn(boolean digestOptIn) {
        this.digestOptIn = digestOptIn;
    }

    public Instant getLastDigestAt() {
        return lastDigestAt;
    }

    public void setLastDigestAt(Instant lastDigestAt) {
        this.lastDigestAt = lastDigestAt;
    }

    public int getBrowseStreakDays() {
        return browseStreakDays;
    }

    public void setBrowseStreakDays(int browseStreakDays) {
        this.browseStreakDays = browseStreakDays;
    }

    public String getLastBrowseDay() {
        return lastBrowseDay;
    }

    public void setLastBrowseDay(String lastBrowseDay) {
        this.lastBrowseDay = lastBrowseDay == null ? "" : lastBrowseDay;
    }

    public void touch() {
        this.lastActiveAt = Instant.now();
    }
}
