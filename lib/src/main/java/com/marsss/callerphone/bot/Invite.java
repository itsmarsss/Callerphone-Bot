package com.marsss.callerphone.bot;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.utils.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Invite implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().replyEmbeds(invite()).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.replyEmbeds(invite()).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "invite` - Get invites and links related to this bot.";
    }

    @Override
    public String[] getTriggers() {
        return "invite,inv".split(",");
    }

    private MessageEmbed invite() {
        return new EmbedBuilder()
                .setColor(Colour.randColor())
                .addField("Add me to your server", "[Invite Link](" + Callerphone.invite + ")", true)
                .addField("Join the Community and Support Server", "[Server Link](" + Callerphone.support + ")", true)
                .addField("Support Us", "[Patreon Link](" + Callerphone.donate + ")", true)
                .setFooter("Have a nice day")
                .build();
    }

}
