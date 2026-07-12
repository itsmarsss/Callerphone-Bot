package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;

/** Command directory states → ExperienceView (plan patterns §28). */
public final class HelpPresenter {
    private HelpPresenter() {
    }

    public static ExperienceView directory(boolean admin) {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Callerphone")
                .description(
                        "Pick a category, or open **`/match`** for your personalized home.\n\n"
                                + "Commands stay one tap away."
                                + (admin ? "\n\n_Moderator tools: DM the bot prefix `help mod`._" : "")
                )
                .footer("Callerphone · /help")
                .actions(
                        ActionSpec.success(HelpComponentIds.category(HelpComponentIds.CAT_MATCH), "Match"),
                        ActionSpec.primary(HelpComponentIds.category(HelpComponentIds.CAT_CALL), "Call"),
                        ActionSpec.secondary(HelpComponentIds.category(HelpComponentIds.CAT_BOTTLE), "Bottles"),
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
                        ActionSpec.success(HelpComponentIds.category(HelpComponentIds.CAT_MATCH), "Match"),
                        ActionSpec.primary(HelpComponentIds.category(HelpComponentIds.CAT_CALL), "Call")
                )
                .build();
    }
}
