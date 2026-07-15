package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BottlePresenterTest {

    @Test
    void emptySeaOffersSendAndFreePaths() {
        ExperienceView view = BottlePresenter.emptySea();
        assertEquals(ExperienceIntent.DISCOVERY, view.intent());
        assertTrue(hasLabel(view, "Send a bottle"));
        assertTrue(hasLabel(view, "Start a call"));
        assertTrue(hasLabel(view, "Discover people"));
    }

    @Test
    void cooldownsOfferAlternateModes() {
        ExperienceView send = BottlePresenter.sendCooldown(5);
        ExperienceView find = BottlePresenter.findCooldown(3);
        assertTrue(hasLabel(send, "Find a bottle"));
        assertTrue(hasLabel(find, "Send a bottle"));
        assertTrue(hasLabel(send, "Start a call") || hasLabel(send, "Discover people"));
        assertTrue(hasLabel(find, "Start a call") || hasLabel(find, "Discover people"));
    }

    @Test
    void genericErrorHasRecovery() {
        ExperienceView err = BottlePresenter.genericError();
        assertTrue(hasLabel(err, "Find a bottle"));
        assertTrue(hasLabel(err, "Send a bottle"));
    }

    @Test
    void identityPickHasAnonymousAndSigned() {
        ExperienceView pick = BottlePresenter.identityPick();
        assertTrue(hasLabel(pick, "Anonymous"));
        assertTrue(hasLabel(pick, "Signed"));
    }

    @Test
    void threadFullWithSignedAuthorOffersInterest() {
        ExperienceView full = BottlePresenter.threadFull("b1", "user99");
        assertTrue(hasLabel(full, "Interested"));
        assertTrue(hasLabel(full, "Keep browsing") || hasLabel(full, "Find another"));
    }

    private static boolean hasLabel(ExperienceView view, String label) {
        return view.actions() != null && view.actions().stream()
                .anyMatch(a -> a != null && label.equals(a.label()));
    }
}
