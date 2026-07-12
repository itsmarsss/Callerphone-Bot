package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.model.CallSession;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.experience.ViewField;

/** Domain call state → ExperienceView. */
public final class CallPresenter {
    private CallPresenter() {
    }

    public static ExperienceView queued(int position, int size) {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Finding a call")
                .description("You're in line. We'll connect this channel automatically when someone is ready.")
                .fields(java.util.List.of(ViewField.of("Queue", "#" + position + " of " + size)))
                .footer("Callerphone Call")
                .actions(ActionSpec.secondary(CallComponentIds.leaveQueue(), "Leave queue"))
                .build();
    }

    public static ExperienceView waiting(int position, int size) {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Still finding a call")
                .description("You're still in line. We'll update this when someone connects.")
                .fields(java.util.List.of(ViewField.of("Queue", "#" + position + " of " + size)))
                .footer("Callerphone Call")
                .actions(ActionSpec.secondary(CallComponentIds.leaveQueue(), "Leave queue"))
                .build();
    }

    public static ExperienceView connected(CallSession session) {
        return connected(session.getId());
    }

    public static ExperienceView connected(String sessionId) {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Call connected")
                .description(
                        "Another chat picked up. Say hello 👋\n\n"
                                + "Messages here go to them."
                )
                .footer("Callerphone Call")
                .actions(
                        ActionSpec.primary(CallComponentIds.prompt(sessionId), "Conversation prompt"),
                        ActionSpec.secondary(CallComponentIds.gameShelf(sessionId), "Start a game"),
                        ActionSpec.success(CallComponentIds.share(sessionId), "Share profile"),
                        ActionSpec.danger(CallComponentIds.report(sessionId), "Report")
                )
                .build();
    }

    public static ExperienceView connectedDm(CallSession session) {
        return connectedDm(session.getId());
    }

    public static ExperienceView connectedDm(String sessionId) {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Call connected")
                .description(
                        "You're chatting through Callerphone. Say hello 👋\n"
                                + "They may be in a server channel or another DM.\n\n"
                                + "Messages you send here go to them."
                )
                .footer("Callerphone Call")
                .actions(
                        ActionSpec.primary(CallComponentIds.prompt(sessionId), "Conversation prompt"),
                        ActionSpec.secondary(CallComponentIds.gameShelf(sessionId), "Start a game"),
                        ActionSpec.success(CallComponentIds.share(sessionId), "Share profile"),
                        ActionSpec.danger(CallComponentIds.report(sessionId), "Report")
                )
                .build();
    }

    public static ExperienceView gameShelf(String sessionId) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Play during this call")
                .description(
                        "Only ready games are listed — nothing half-baked.\n\n"
                                + "**Tic-Tac-Toe** · Ready\n"
                                + "_Connect Four & Battleship stay off this shelf until live._"
                )
                .actions(
                        ActionSpec.success(CallComponentIds.gameTtt(sessionId), "Play Tic-Tac-Toe"),
                        ActionSpec.secondary(CallComponentIds.prompt(sessionId), "Conversation prompt")
                )
                .build();
    }

    public static ExperienceView gameProposal(String sessionId, String proposerUserId) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Game challenge")
                .description("The other side wants to play **Tic-Tac-Toe**.\nThe call stays connected.")
                .actions(
                        ActionSpec.success(CallComponentIds.gameAccept(sessionId, proposerUserId), "Play"),
                        ActionSpec.secondary(CallComponentIds.gameDecline(sessionId), "Not now")
                )
                .build();
    }

    public static ExperienceView gameStarted(String sessionId) {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Tic-Tac-Toe started")
                .description(
                        "Boards appear in both call channels. Play there — the call stays connected."
                )
                .footer("Callerphone Call")
                .actions(
                        ActionSpec.primary(CallComponentIds.prompt(sessionId), "Conversation prompt"),
                        ActionSpec.secondary(CallComponentIds.share(sessionId), "Share profile")
                )
                .build();
    }

    public static ExperienceView queuedDm(int position, int size) {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Finding a call")
                .description("You're in line. You may connect with a server channel or another DM.")
                .fields(java.util.List.of(ViewField.of("Queue", "#" + position + " of " + size)))
                .footer("Callerphone Call")
                .actions(ActionSpec.secondary(CallComponentIds.leaveQueue(), "Leave queue"))
                .build();
    }

    public static ExperienceView waitingDm(int position, int size) {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Still finding a call")
                .description("You're still in line. You may connect with a server channel or another DM.")
                .fields(java.util.List.of(ViewField.of("Queue", "#" + position + " of " + size)))
                .footer("Callerphone Call")
                .actions(ActionSpec.secondary(CallComponentIds.leaveQueue(), "Leave queue"))
                .build();
    }

    public static ExperienceView endConfirm() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("End this call?")
                .description("Both sides will disconnect. You can start another call right after.")
                .actions(
                        ActionSpec.danger(CallComponentIds.endConfirm(), "End call"),
                        ActionSpec.secondary(CallComponentIds.endCancel(), "Keep talking"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                        com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                ),
                                "Find a bottle"
                        )
                )
                .build();
    }

    public static ExperienceView conversationPrompt(String prompt) {
        return conversationPrompt(prompt, null);
    }

    public static ExperienceView conversationPrompt(String prompt, String sessionId) {
        ExperienceView.Builder b = ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Conversation prompt")
                .description("_" + prompt + "_")
                .footer("Optional — say anything you like");
        if (sessionId != null && !sessionId.isBlank()) {
            b.actions(
                    ActionSpec.secondary(CallComponentIds.prompt(sessionId), "Another prompt"),
                    ActionSpec.primary(CallComponentIds.gameShelf(sessionId), "Start a game")
            );
        }
        return b.build();
    }

    public static final String[] PROMPTS = {
            "What's a song you could play on loop for a week?",
            "What's your comfort game or show right now?",
            "If you had a free Sunday, what would you do?",
            "What's something small that made your week better?",
            "What are you learning or trying lately?",
            "Coffee shop or late-night snack — which are you?",
            "What's a hobby you'd love to talk about more?",
            "If we had ten minutes, what should we debate for fun?"
    };

    public static String randomPrompt() {
        return PROMPTS[java.util.concurrent.ThreadLocalRandom.current().nextInt(PROMPTS.length)];
    }

    public static ExperienceView ended(String sessionId) {
        return ended(sessionId, -1, -1);
    }

    public static ExperienceView ended(String sessionId, long minutes, int messageCount) {
        ExperienceView.Builder b = ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Call ended")
                .description(statsLine(minutes, messageCount, "Thanks for chatting."));
        if (sessionId != null && !sessionId.isBlank()) {
            b.actions(
                    ActionSpec.primary(CallComponentIds.again(sessionId), "Call again"),
                    ActionSpec.success(
                            com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                    com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                            ),
                            "Find a bottle"
                    ),
                    ActionSpec.secondary(
                            com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                    com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                            ),
                            "Discover people"
                    ),
                    ActionSpec.danger(CallComponentIds.report(sessionId), "Report")
            );
        }
        return b.build();
    }

    public static ExperienceView ended() {
        return ended(null, -1, -1);
    }

    public static ExperienceView peerHungUp(String sessionId) {
        return peerHungUp(sessionId, -1, -1);
    }

    public static ExperienceView peerHungUp(String sessionId, long minutes, int messageCount) {
        ExperienceView.Builder b = ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Call ended")
                .description(statsLine(minutes, messageCount, "The other side hung up."));
        if (sessionId != null && !sessionId.isBlank()) {
            b.actions(
                    ActionSpec.primary(CallComponentIds.again(sessionId), "Call again"),
                    ActionSpec.success(
                            com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                    com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                            ),
                            "Find a bottle"
                    ),
                    ActionSpec.secondary(
                            com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                    com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                            ),
                            "Discover people"
                    ),
                    ActionSpec.danger(CallComponentIds.report(sessionId), "Report")
            );
        }
        return b.build();
    }

    private static String statsLine(long minutes, int messageCount, String base) {
        if (minutes < 0 || messageCount < 0) {
            return base;
        }
        String time = minutes <= 0 ? "under a minute" : minutes + (minutes == 1 ? " minute" : " minutes");
        return base + "\n\n" + time + " · " + messageCount + " message" + (messageCount == 1 ? "" : "s");
    }

    public static ExperienceView leftQueue() {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Left the queue")
                .description("This channel is no longer waiting for a call.")
                .actions(
                        ActionSpec.primary(CallComponentIds.again("_"), "Find a call"),
                        ActionSpec.success(
                                com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                        com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                ),
                                "Find a bottle"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        )
                )
                .build();
    }

    public static ExperienceView noCall() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("No active call")
                .description("Start a random conversation with another server or in DMs.")
                .actions(
                        ActionSpec.primary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.success(
                                com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                        com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                ),
                                "Find a bottle"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        )
                )
                .build();
    }

    public static ExperienceView conflict() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Already on a call")
                .description("This channel is already connected. End it before starting another.")
                .actions(
                        ActionSpec.danger(CallComponentIds.endConfirm(), "End call"),
                        ActionSpec.secondary(CallComponentIds.endCancel(), "Keep talking"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                        com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                ),
                                "Find a bottle"
                        )
                )
                .build();
    }

    public static ExperienceView warn(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }

    public static ExperienceView reportPrompt() {
        return ExperienceView.builder(ExperienceIntent.SAFETY)
                .title("Report this call")
                .description("Choose the closest reason. Recent messages will be attached for review.")
                .ephemeral(true)
                .build();
    }

    /** Empty/error recovery after a failed or missing call. */
    public static ExperienceView warnRecover(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title(title)
                .description(description)
                .actions(
                        ActionSpec.primary(CallComponentIds.again("_"), "Try a call"),
                        ActionSpec.success(
                                com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                        com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                ),
                                "Find a bottle"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_HOME, "_"
                                ),
                                "Inbox"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView success(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }

    /** Success while still in a call — keep the session controls handy. */
    public static ExperienceView successInCall(String title, String description, String sessionId) {
        ExperienceView.Builder b = ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title(title)
                .description(description)
                .ephemeral(true);
        if (sessionId != null && !sessionId.isBlank()) {
            b.actions(
                    ActionSpec.primary(CallComponentIds.prompt(sessionId), "Conversation prompt"),
                    ActionSpec.secondary(CallComponentIds.gameShelf(sessionId), "Start a game"),
                    ActionSpec.success(CallComponentIds.share(sessionId), "Share profile")
            );
        }
        return b.build();
    }

    public static ExperienceView info(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }
}
