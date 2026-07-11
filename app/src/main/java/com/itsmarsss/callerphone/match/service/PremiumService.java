package com.itsmarsss.callerphone.match.service;

/**
 * Centralized capability checks. Purchases remain disabled until approved;
 * everyone currently receives free limits.
 */
public final class PremiumService {
    public boolean isPremium(String userId) {
        return false;
    }

    public int dailyDiscoveries(String userId) {
        return isPremium(userId) ? MatchLimits.PREMIUM_DAILY_DISCOVERIES : MatchLimits.FREE_DAILY_DISCOVERIES;
    }

    public int dailyInterests(String userId) {
        return isPremium(userId) ? MatchLimits.PREMIUM_DAILY_INTERESTS : MatchLimits.FREE_DAILY_INTERESTS;
    }

    public int activeConversations(String userId) {
        return isPremium(userId) ? MatchLimits.PREMIUM_ACTIVE_CONVERSATIONS : MatchLimits.FREE_ACTIVE_CONVERSATIONS;
    }

    public boolean canSeeIncomingInterest(String userId) {
        return isPremium(userId);
    }

    public boolean canUndoSkip(String userId) {
        return isPremium(userId);
    }
}
