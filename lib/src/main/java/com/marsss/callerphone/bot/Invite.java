package com.marsss.callerphone.bot;

import java.awt.Color;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.utils.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Invite implements Command {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Color COLOR = Colour.randColor();
        EmbedBuilder InvEmd = new EmbedBuilder()
                .setColor(COLOR)
                .addField("Add me to your server", "[Invite Link](" + Callerphone.invite + ")", true)
                .addField("Join the Community and Support Server", "[Server Link](" + Callerphone.support + ")", true)
                .addField("Support Us", "[Patreon Link](" + Callerphone.donate + ")", true)
                .setFooter("Have a nice day");

        e.getMessage().replyEmbeds(InvEmd.build()).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "invite` - Get invites and links related to this bot.";
    }

    @Override
    public String[] getTriggers() {
        return "invite,inv".split(",");
    }

}
