package com.marsss.callerphone.bot;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.utils.Colour;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Invite implements ICommand {

    @Override
    public void runCommand(MessageReceivedEvent e) {
        e.getMessage().replyEmbeds(invite()).queue();
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(invite()).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "invite` - Get invites and links related to this bot.";
    }

    @Override
    public String[] getTriggers() {
        return "invite,inv".split(",");
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

}
