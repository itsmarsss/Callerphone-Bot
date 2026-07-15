package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.discord.match.MatchPresenter;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Snapshot-style checks for highest-value Match presenter states.
 * Guards recovery CTAs and home state matrix against silent regressions.
 */
class MatchPresenterTest {

    @Test
    void notEnrolledHomeOffersCreateAndFreePaths() {
        ExperienceView home = MatchPresenter.home("Sam", 0, 10, false, false);
        assertEquals(ExperienceIntent.SOCIAL, home.intent());
        assertTrue(hasLabel(home, "Create profile") || hasLabel(home, "Create"));
        assertTrue(hasLabel(home, "Start a call"));
        assertTrue(hasLabel(home, "Find a bottle") || hasLabel(home, "Find bottles"));
    }

    @Test
    void incompleteHomeOffersSetup() {
        ExperienceView home = MatchPresenter.home("Sam", 0, 10, false, true, false, 0, 0);
        assertEquals(ExperienceIntent.PROGRESS, home.intent());
        assertTrue(hasLabel(home, "Finish setup") || hasLabel(home, "Continue setup")
                || hasLabel(home, "Photo") || hasLabel(home, "Add photo"));
    }

    @Test
    void pausedHomeOffersResume() {
        ExperienceView home = MatchPresenter.home("Sam", 0, 5, true, true, true, 0, 0);
        assertTrue(hasLabel(home, "Resume profile") || hasLabel(home, "Resume"));
        assertTrue(hasLabel(home, "Open chats") || hasLabel(home, "Start a call"));
    }

    @Test
    void unreadChatsPrioritizeOpenChats() {
        ExperienceView home = MatchPresenter.home("Sam", 3, 5, true, true, false, 0, 0);
        assertTrue(hasLabel(home, "Open chats"));
        assertEquals("Open chats", firstActionLabel(home));
    }

    @Test
    void dailyLimitOffersFreePathsAndPremium() {
        ExperienceView limit = MatchPresenter.dailyLimitHome("Sam");
        assertEquals(ExperienceIntent.PREMIUM, limit.intent());
        assertTrue(hasLabel(limit, "Open chats"));
        assertTrue(hasLabel(limit, "Find a bottle"));
        assertTrue(hasLabel(limit, "Start a call"));
        assertTrue(hasLabel(limit, "About Premium"));
    }

    @Test
    void softLimitSameRecoveryFamily() {
        ExperienceView limit = MatchPresenter.softLimit("Not right now", "Interest limit");
        assertEquals(ExperienceIntent.PREMIUM, limit.intent());
        assertTrue(hasLabel(limit, "Open chats"));
        assertTrue(hasLabel(limit, "About Premium"));
    }

    @Test
    void serviceFailedAndDoneHaveRecoveryCtas() {
        ExperienceView fail = MatchPresenter.serviceFailed("Nope");
        ExperienceView ok = MatchPresenter.serviceDone("Saved");
        assertTrue(hasLabel(fail, "Discover") || hasLabel(fail, "Home"));
        assertTrue(hasLabel(ok, "Discover"));
        assertTrue(hasLabel(ok, "Start a call"));
        assertTrue(hasLabel(fail, "Find a bottle") || hasLabel(ok, "Find a bottle"));
    }

    @Test
    void expiredActionHasMultiModeRecovery() {
        ExperienceView expired = MatchPresenter.expired();
        assertEquals(ExperienceIntent.WARNING, expired.intent());
        assertTrue(hasLabel(expired, "Discover") || hasLabel(expired, "Home"));
        assertTrue(hasLabel(expired, "Chats") || hasLabel(expired, "Open chats") || hasLabel(expired, "Start a call"));
    }

    @Test
    void chatSelectedReflectsGameAndConnectState() {
        ExperienceView available = MatchPresenter.chatSelected(
                "Alex", "Hi", "c1", ConversationStage.MEDIATED,
                false, false, MatchPresenter.ChatGameUi.AVAILABLE, true
        );
        assertTrue(hasLabel(available, "Play a game"));
        assertTrue(hasLabel(available, "Request connect"));
        assertTrue(hasLabel(available, "Back to inbox"));

        ExperienceView acceptGame = MatchPresenter.chatSelected(
                "Alex", "Hi", "c1", ConversationStage.MEDIATED,
                false, false, MatchPresenter.ChatGameUi.ACCEPT_THEIRS, false
        );
        assertTrue(hasLabel(acceptGame, "Play Tic-Tac-Toe"));
        assertTrue(hasLabel(acceptGame, "Decline game"));

        ExperienceView waitingConnect = MatchPresenter.chatSelected(
                "Alex", "Hi", "c1", ConversationStage.CONNECT_PENDING,
                true, false, MatchPresenter.ChatGameUi.AVAILABLE, false
        );
        assertTrue(hasLabel(waitingConnect, "Waiting for connect…")
                || waitingConnect.actions().stream().anyMatch(ActionSpec::disabled));
    }

    @Test
    void rewardsCatalogIsHonestAboutCredits() {
        ExperienceView rewards = MatchPresenter.rewardsCatalog(3, 100, null);
        assertNotNull(rewards.description());
        assertTrue(rewards.description().toLowerCase().contains("nothing to spend")
                || rewards.description().toLowerCase().contains("not spendable")
                || rewards.description().toLowerCase().contains("future"));
        assertTrue(hasLabel(rewards, "About Premium") || hasLabel(rewards, "Discover"));
    }

    @Test
    void connectedOffersOpenChatAndGame() {
        ExperienceView connected = MatchPresenter.connected("Alex", "Favorite song?", "c99");
        assertEquals(ExperienceIntent.SOCIAL, connected.intent());
        assertTrue(hasLabel(connected, "Open chat"));
        assertTrue(hasLabel(connected, "Play a game"));
    }

    @Test
    void chatsEmptyOffersDiscoverCallBottle() {
        ExperienceView empty = MatchPresenter.chatsEmpty();
        assertTrue(hasLabel(empty, "Discover") || hasLabel(empty, "Discover people"));
        assertTrue(hasLabel(empty, "Start a call"));
        assertTrue(hasLabel(empty, "Find a bottle"));
    }

    private static boolean hasLabel(ExperienceView view, String label) {
        if (view.actions() == null) {
            return false;
        }
        return view.actions().stream()
                .anyMatch(a -> a != null && a.label() != null && a.label().equals(label));
    }

    private static String firstActionLabel(ExperienceView view) {
        return view.actions().isEmpty() ? null : view.actions().get(0).label();
    }
}
