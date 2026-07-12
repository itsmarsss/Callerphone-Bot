package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.events.entitlement.EntitlementCreateEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discord Premium Apps entitlement → {@link com.itsmarsss.callerphone.match.service.PremiumService}.
 * No-ops until {@code premiumSkuId} is set in config and the SKU is approved.
 */
public final class OnEntitlementEvent extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(OnEntitlementEvent.class);

    @Override
    public void onEntitlementCreate(@NotNull EntitlementCreateEvent event) {
        apply(event.getEntitlement(), true);
    }

    @Override
    public void onEntitlementDelete(@NotNull EntitlementDeleteEvent event) {
        apply(event.getEntitlement(), false);
    }

    @Override
    public void onEntitlementUpdate(@NotNull EntitlementUpdateEvent event) {
        // Treat updates as still active; delete handles revocation.
        apply(event.getEntitlement(), true);
    }

    private static void apply(Entitlement entitlement, boolean active) {
        if (entitlement == null || !ApplicationContext.isReady()) {
            return;
        }
        String userId = entitlement.getUserId();
        String skuId = entitlement.getSkuId();
        try {
            ApplicationContext.get().premium().syncEntitlement(userId, skuId, active);
            logger.info("Premium entitlement {} for user {} sku {}",
                    active ? "grant" : "revoke", userId, skuId);
            ApplicationContext.get().analytics().track(
                    userId,
                    active ? "premium_entitlement_grant" : "premium_entitlement_revoke",
                    skuId
            );
        } catch (Exception e) {
            logger.warn("Failed to sync entitlement for {}: {}", userId, e.getMessage());
        }
    }
}
