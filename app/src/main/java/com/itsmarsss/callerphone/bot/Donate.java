package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Plan §32: concrete support outcomes. */
public class Donate implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(donate()).setEphemeral(true).queue();
    }

    private MessageEmbed donate() {
        return new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("Support Callerphone")
                .setDescription(
                        "Help cover hosting, moderation tools, and new social features.\n\n"
                                + "[Support the project](" + Callerphone.config.getDonateLink() + ")"
                )
                .setFooter("Thank you")
                .build();
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
