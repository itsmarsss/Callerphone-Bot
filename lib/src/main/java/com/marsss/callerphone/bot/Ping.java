package com.marsss.callerphone.bot;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Ping implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        Callerphone.jda.getRestPing().queue(
                (ping) -> e.replyFormat(Response.PING_TEMPLATE.toString(), ping, Callerphone.jda.getGatewayPing()).setEphemeral(true).queue());
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "ping` - Gets bot ping.";
    }

    @Override
    public String[] getTriggers() {
        return "ping,pong".split(",");
    }
}
