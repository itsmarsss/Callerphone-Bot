package com.marsss.callerphone.bot;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Ping implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        Callerphone.jda.getRestPing().queue(
                (ping) -> e.getMessage().replyFormat(Response.PING_TEMPLATE.toString(), ping, Callerphone.jda.getGatewayPing()).queue());
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        Callerphone.jda.getRestPing().queue(
                (ping) -> e.replyFormat(Response.PING_TEMPLATE.toString(), ping, Callerphone.jda.getGatewayPing()).setEphemeral(true).queue());
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "ping` - Gets bot ping.";
    }

    @Override
    public String[] getTriggers() {
        return "ping,pong".split(",");
    }
}
