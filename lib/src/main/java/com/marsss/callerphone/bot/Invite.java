package com.marsss.callerphone.bot;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.utils.Colour;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Invite implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(invite()).queue();
    }

    private MessageEmbed invite() {
        return new EmbedBuilder()
                .setColor(Colour.randColor())
                .addField("Add me to your server", "[Invite Link](" + Callerphone.config.getBotInviteLink() + ")", true)
                .addField("Join the Community and Support Server", "[Server Link](" + Callerphone.config.getSupportServer() + ")", true)
                .addField("Support Us", "[Patreon Link](" + Callerphone.config.getDonateLink() + ")", true)
                .setFooter("Have a nice day")
                .build();
    }

    @Override
    public String getHelp() {
        return "</invite:1075168882702164068> - Get invites and links related to this bot.";
    }

    @Override
    public String[] getTriggers() {
        return "invite,inv".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
