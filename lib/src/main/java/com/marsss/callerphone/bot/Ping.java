package com.marsss.callerphone.bot;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Ping implements Command {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        Callerphone.jda.getRestPing().queue(
                (ping) -> e.getMessage().replyFormat("**Reset ping:** %sms \n**WS ping:** %sms", ping, Callerphone.jda.getGatewayPing()).queue());
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "ping` - Gets the bot's ping.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "ping` - Gets the bot's ping.";
    }

    @Override
    public String[] getTriggers() {
        return "ping,pong".split(",");
    }
}
