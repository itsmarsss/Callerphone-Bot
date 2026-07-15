package com.itsmarsss.callerphone.call;

import com.itsmarsss.callerphone.call.discord.CallPresenter;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CallPresenterTest {

    @Test
    void warnRecoverOffersMultiModeCtas() {
        ExperienceView view = CallPresenter.warnRecover("Couldn't connect", "Try again.");
        assertEquals(ExperienceIntent.WARNING, view.intent());
        assertTrue(hasLabel(view, "Try a call"));
        assertTrue(hasLabel(view, "Find a bottle"));
        assertTrue(hasLabel(view, "Discover people"));
    }

    @Test
    void gameShelfOnlyListsReadyGames() {
        ExperienceView shelf = CallPresenter.gameShelf("sess1");
        assertTrue(hasLabel(shelf, "Play Tic-Tac-Toe"));
        assertFalse(hasLabel(shelf, "Connect Four"));
        assertFalse(hasLabel(shelf, "Battleship"));
    }

    @Test
    void connectedHasPromptGameShareReport() {
        ExperienceView connected = CallPresenter.connected("sess1");
        assertEquals(ExperienceIntent.SUCCESS, connected.intent());
        assertTrue(hasLabel(connected, "Conversation prompt"));
        assertTrue(hasLabel(connected, "Start a game"));
        assertTrue(hasLabel(connected, "Share profile"));
        assertTrue(hasLabel(connected, "Report"));
    }

    @Test
    void endedOffersCallAgainAndDiscover() {
        ExperienceView ended = CallPresenter.ended("sess1", 2, 10);
        assertTrue(hasLabel(ended, "Call again"));
        assertTrue(hasLabel(ended, "Find a bottle"));
        assertTrue(hasLabel(ended, "Discover people"));
    }

    @Test
    void noCallAndLeftQueueHaveRecovery() {
        assertTrue(hasLabel(CallPresenter.noCall(), "Start a call"));
        assertTrue(hasLabel(CallPresenter.leftQueue(), "Find a call") || hasLabel(CallPresenter.leftQueue(), "Start a call"));
        assertTrue(hasLabel(CallPresenter.noCall(), "Discover people")
                || hasLabel(CallPresenter.noCall(), "Find a bottle"));
    }

    private static boolean hasLabel(ExperienceView view, String label) {
        return view.actions() != null && view.actions().stream()
                .anyMatch(a -> a != null && label.equals(a.label()));
    }
}
