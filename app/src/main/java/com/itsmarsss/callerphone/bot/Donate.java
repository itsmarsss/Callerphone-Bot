package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Plan §32: concrete support outcomes; Premium is separate value, not competing guilt. */
public class Donate implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String donate = Callerphone.config.getDonateLink();
        java.util.List<ActionSpec> actions = new java.util.ArrayList<>();
        if (donate != null && !donate.isBlank()) {
            actions.add(ActionSpec.link(donate, "Support the project"));
        }
        actions.add(ActionSpec.secondary(
                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_PREMIUM, "_"
                ),
                "About Premium"
        ));
        actions.add(ActionSpec.success(
                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_HOME, "_"
                ),
                "Open home"
        ));
        actions.add(ActionSpec.secondary(
                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                "Start a call"
        ));
        e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Support Callerphone")
                .description(
                        "Help cover hosting, moderation tools, and new social features.\n\n"
                                + "Every bit keeps random calls, Match, and bottles online for more people.\n\n"
                                + "_Premium (when live) is a product upgrade — donations keep the free tier healthy._"
                )
                .footer("Thank you")
                .actions(actions)
                .build())).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "`/donate` support the project";
    }

    @Override
    public String getName() {
        return "donate";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Support the project")
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
