package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;

/** Bottle domain states → ExperienceView (plan patterns §25–27). */
public final class BottlePresenter {
    private BottlePresenter() {
    }

    public static ExperienceView identityPick() {
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("How should this bottle appear?")
                .description(
                        "**Anonymous** hides your Social profile.\n"
                                + "**Signed** shows your name — still no direct messages unless it's mutual interest later."
                )
                .actions(
                        ActionSpec.secondary(BottleComponentIds.of(BottleComponentIds.ACTION_ID_ANON, "_"), "Anonymous"),
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_ID_SIGN, "_"), "Signed")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView sent() {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Bottle sent 🌊")
                .description(
                        "It's now floating for someone else to find.\n\n"
                                + "Replies show up in your inbox when someone adds a page."
                )
                .actions(
                        ActionSpec.primary(BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"), "Send another"),
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView replySent() {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Reply added")
                .description("Your page is on the bottle. Others in the thread will see it in their inbox.")
                .actions(
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find another"),
                        ActionSpec.primary(BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"), "Send a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView sendCooldown(long minutes) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Not yet")
                .description("Your next bottle can launch in **" + minutes + "** minute(s).")
                .actions(
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView findCooldown(long minutes) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Cooldown")
                .description("Try finding another bottle in **" + minutes + "** minute(s).")
                .actions(
                        ActionSpec.primary(BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"), "Send a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView emptySea() {
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("The water is quiet")
                .description(
                        "No new bottles are available right now.\n\n"
                                + "Cast one yourself, or try a call while the sea rests."
                )
                .actions(
                        ActionSpec.primary(BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"), "Send a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView threadFull(String bottleId, String signedAuthorId) {
        ExperienceView.Builder b = ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Thread is full")
                .description(
                        "This bottle hit the reply limit. "
                                + "Keep talking through Match if you're both interested — nothing is shared unless it's mutual."
                )
                .ephemeral(true);
        if (signedAuthorId != null && !signedAuthorId.isBlank()) {
            b.actions(
                    ActionSpec.success(
                            com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                    com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_BOTTLE_INTEREST,
                                    signedAuthorId
                            ),
                            "Interested"
                    ),
                    ActionSpec.secondary(
                            BottleComponentIds.of(BottleComponentIds.ACTION_KEEP_ANON, bottleId),
                            "Keep browsing"
                    )
            );
        } else {
            b.actions(
                    ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find another"),
                    ActionSpec.secondary(BottleComponentIds.of(BottleComponentIds.ACTION_KEEP_ANON, bottleId), "Okay")
            );
        }
        return b.build();
    }

    public static ExperienceView replyIdentity(String bottleId) {
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("Add a page")
                .description("How should this reply appear?")
                .actions(
                        ActionSpec.secondary(
                                BottleComponentIds.of(BottleComponentIds.ACTION_REPLY_ANON, bottleId),
                                "Anonymous"
                        ),
                        ActionSpec.success(
                                BottleComponentIds.of(BottleComponentIds.ACTION_REPLY_SIGN, bottleId),
                                "Signed"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView keepBrowsing() {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("All good")
                .description("You can still read the bottle. Find another whenever you're ready.")
                .actions(
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView genericError() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Couldn't load that bottle")
                .description("Nothing was used. Try finding another, or cast one of your own.")
                .actions(
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.primary(BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"), "Send a bottle")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView messageFlagged() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Message needs a tweak")
                .description(
                        "Remove links, pings, or blocked language, then try again.\n"
                                + "Your bottle wasn't sent."
                )
                .actions(
                        ActionSpec.primary(BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"), "Try again"),
                        ActionSpec.secondary(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView sendRateLimited() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Send limit reached")
                .description("You've sent as many bottles as you can for now. Find one while you wait.")
                .actions(
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        ),
                        ActionSpec.secondary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        )
                )
                .ephemeral(true)
                .build();
    }
}
