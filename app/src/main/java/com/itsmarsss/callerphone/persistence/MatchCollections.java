package com.itsmarsss.callerphone.persistence;

/**
 * New Match/Social collections only. Existing users/chats/mibs/pools are untouched.
 */
public final class MatchCollections {
    public static final String MATCH_USERS = "match_users";
    public static final String MATCH_PROFILES = "match_profiles";
    public static final String MATCH_DECISIONS = "match_decisions";
    public static final String MATCHES = "matches";
    public static final String MATCH_CONVERSATIONS = "match_conversations";
    public static final String MATCH_MESSAGES = "match_messages";
    public static final String BLOCKS = "blocks";
    public static final String REPORTS = "reports";
    public static final String SANCTIONS = "sanctions";
    public static final String CONSENT_EVENTS = "consent_events";
    public static final String AUDIT_EVENTS = "audit_events";
    public static final String PRODUCT_EVENTS = "product_events";
    public static final String BROWSE_SESSIONS = "browse_sessions";
    public static final String SOCIAL_INBOX = "social_inbox";
    public static final String SCHEMA_MIGRATIONS = "schema_migrations";

    private MatchCollections() {
    }
}
