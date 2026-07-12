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

/** Plan §32: concrete support outcomes. */
public class Donate implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String donate = Callerphone.config.getDonateLink();
        e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Support Callerphone")
                .description(
                        "Help cover hosting, moderation tools, and new social features.\n\n"
                                + "Every bit keeps random calls, Match, and bottles online for more people."
                )
                .footer("Thank you")
                .actions(ActionSpec.link(donate, "Support the project"))
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
