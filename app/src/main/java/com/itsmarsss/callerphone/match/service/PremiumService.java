package com.itsmarsss.callerphone.match.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized capability checks.
 * Purchases disabled until Discord Premium Apps is approved;
 * entitlements can be force-granted for testing via {@link #grant(String)}.
 */
public final class PremiumService {
    private final Map<String, Boolean> entitlements = new ConcurrentHashMap<>();

    public boolean isPremium(String userId) {
        return Boolean.TRUE.equals(entitlements.get(userId));
    }

    /** Test/staff grant until native Discord entitlement sync lands. */
    public void grant(String userId) {
        entitlements.put(userId, true);
    }

    public void revoke(String userId) {
        entitlements.remove(userId);
    }

    /** Sync hook for future Discord Premium Apps SKUs. */
    public void syncEntitlement(String userId, String skuId, boolean active) {
        if (active) {
            grant(userId);
        } else {
            revoke(userId);
        }
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

    public int dailyUndos(String userId) {
        return isPremium(userId) ? MatchLimits.PREMIUM_DAILY_UNDOS : MatchLimits.FREE_DAILY_UNDOS;
    }

    /** Anyone can know interest exists; full name list is Premium. */
    public boolean canSeeIncomingInterest(String userId) {
        return true;
    }

    /** Full incoming-interest list with names. */
    public boolean canSeeIncomingInterestNames(String userId) {
        return isPremium(userId);
    }

    public boolean canUndoSkip(String userId) {
        return true; // free tier has limited undos; premium has more
    }

    public boolean hasAdvancedFilters(String userId) {
        return isPremium(userId);
    }

    public String upsellForLimit(String limitName) {
        return UpsellCopy.forLimit(limitName);
    }
}
