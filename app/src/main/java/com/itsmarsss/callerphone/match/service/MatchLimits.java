package com.itsmarsss.callerphone.match.service;

/** Server-side entitlement defaults. Premium can raise these later. */
public final class MatchLimits {
    public static final int FREE_DAILY_DISCOVERIES = 15;
    public static final int FREE_DAILY_INTERESTS = 5;
    public static final int FREE_ACTIVE_CONVERSATIONS = 2;
    public static final int FREE_DAILY_UNDOS = 1;
    public static final int PREMIUM_DAILY_DISCOVERIES = 60;
    public static final int PREMIUM_DAILY_INTERESTS = 25;
    public static final int PREMIUM_ACTIVE_CONVERSATIONS = 8;
    public static final int PREMIUM_DAILY_UNDOS = 10;
    public static final long SKIP_EXPIRE_DAYS = 14;
    public static final long CONNECT_EXPIRE_HOURS = 48;
    public static final long CHAT_IDLE_MINUTES = 30;
    public static final long NUDGE_AFTER_HOURS = 48;
    public static final int REPORT_EVIDENCE_MESSAGES = 20;
    public static final int MAX_PROFILE_PHOTOS = 3;
    public static final String TERMS_VERSION = "match-terms-v1";
    public static final String PRIVACY_VERSION = "match-privacy-v1";

    private MatchLimits() {
    }
}
