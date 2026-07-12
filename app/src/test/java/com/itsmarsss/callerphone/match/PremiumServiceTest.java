package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.service.PremiumService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PremiumServiceTest {

    @Test
    void grantAndRevokeWithoutConfiguredSku() {
        PremiumService premium = new PremiumService();
        assertFalse(premium.isPremium("u1"));
        premium.syncEntitlement("u1", "any-sku", true);
        assertTrue(premium.isPremium("u1"));
        premium.syncEntitlement("u1", "any-sku", false);
        assertFalse(premium.isPremium("u1"));
    }

    @Test
    void staffGrantWorks() {
        PremiumService premium = new PremiumService();
        premium.grant("staff");
        assertTrue(premium.isPremium("staff"));
        premium.revoke("staff");
        assertFalse(premium.isPremium("staff"));
    }

    @Test
    void limitsDifferByEntitlement() {
        PremiumService premium = new PremiumService();
        int freeDisc = premium.dailyDiscoveries("free");
        premium.grant("paid");
        int paidDisc = premium.dailyDiscoveries("paid");
        assertTrue(paidDisc > freeDisc);
        assertTrue(premium.canSeeIncomingInterestNames("paid"));
        assertFalse(premium.canSeeIncomingInterestNames("free"));
    }

    @Test
    void purchasesLiveFalseWithoutConfig() {
        PremiumService premium = new PremiumService();
        // Without Callerphone.config premiumSkuId, purchases are not live
        assertFalse(premium.purchasesLive());
        assertEquals("", premium.configuredSkuId());
    }
}
