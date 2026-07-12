package com.itsmarsss.callerphone.match.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized capability checks.
 * Discord Premium Apps SKU id is optional in config ({@code premiumSkuId}).
 * Entitlements can also be force-granted for testing via {@link #grant(String)}.
 */
public final class PremiumService {
    private final Map<String, Boolean> entitlements = new ConcurrentHashMap<>();
    private final Map<String, String> entitledSkuByUser = new ConcurrentHashMap<>();

    public boolean isPremium(String userId) {
        return Boolean.TRUE.equals(entitlements.get(userId));
    }

    /** Test/staff grant until native Discord entitlement sync lands. */
    public void grant(String userId) {
        entitlements.put(userId, true);
    }

    public void revoke(String userId) {
        entitlements.remove(userId);
        entitledSkuByUser.remove(userId);
    }

    /**
     * Sync Discord Premium Apps entitlement for the configured SKU.
     * Unknown SKUs are ignored so test SKUs don't grant incorrectly.
     */
    public void syncEntitlement(String userId, String skuId, boolean active) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String configured = configuredSkuId();
        if (configured.isBlank()) {
            // No SKU configured yet — still honor explicit grants for staff testing.
            if (active) {
                grant(userId);
            } else {
                revoke(userId);
            }
            return;
        }
        if (skuId == null || !configured.equals(skuId.trim())) {
            return;
        }
        if (active) {
            grant(userId);
            entitledSkuByUser.put(userId, configured);
        } else {
            revoke(userId);
        }
    }

    public String configuredSkuId() {
        try {
            if (com.itsmarsss.callerphone.Callerphone.config != null) {
                return com.itsmarsss.callerphone.Callerphone.config.getPremiumSkuId().trim();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public boolean purchasesLive() {
        return !configuredSkuId().isBlank();
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
