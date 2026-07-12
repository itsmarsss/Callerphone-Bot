package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.utils.EmbedHelpers;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Plan §31: invite with clear link actions. */
public class Invite implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(invite()).setEphemeral(true).queue();
    }

    private MessageEmbed invite() {
        return new EmbedBuilder()
                .setColor(EmbedHelpers.randColor())
                .setTitle("Bring Callerphone with you")
                .setDescription("Add Callerphone to a server or join the community.")
                .addField("Add to a server", "[Invite](" + Callerphone.config.getBotInviteLink() + ")", true)
                .addField("Community", "[Support server](" + Callerphone.config.getSupportServer() + ")", true)
                .addField("Support us", "[Donate](" + Callerphone.config.getDonateLink() + ")", true)
                .setFooter("Glad you're here")
                .build();
    }

    @Override
    public String getHelp() {
        return "`/invite` bot and support links";
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Bot and support links")
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
