package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.call.discord.CallComponentIds;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.msginbottle.BottleComponentIds;

/** Command directory states → ExperienceView (plan patterns §28). */
public final class HelpPresenter {
    private HelpPresenter() {
    }

    public static ExperienceView directory(boolean admin) {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Callerphone")
                .description(
                        "What do you want to do?\n\n"
                                + "Categories explain commands. Buttons jump into the product."
                                + (admin ? "\n\n_Moderator tools: DM the bot prefix `help mod`._" : "")
                )
                .footer("Callerphone · /help")
                .actions(
                        ActionSpec.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                                "Meet people"
                        ),
                        ActionSpec.primary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.secondary(
                                BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"),
                                "Find bottles"
                        ),
                        ActionSpec.secondary(HelpComponentIds.category(HelpComponentIds.CAT_GAMES), "Games"),
                        ActionSpec.secondary(HelpComponentIds.category(HelpComponentIds.CAT_BOT), "Bot")
                )
                .build();
    }

    public static ExperienceView category(String title, String body) {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title(title)
                .description(body)
                .footer("Callerphone · /help")
                .actions(
                        ActionSpec.secondary(HelpComponentIds.home(), "All categories"),
                        ActionSpec.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                                "Discover"
                        ),
                        ActionSpec.primary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.secondary(
                                BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"),
                                "Find a bottle"
                        )
                )
                .build();
    }
}
