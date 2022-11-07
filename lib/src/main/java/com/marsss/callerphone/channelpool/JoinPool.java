package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class JoinPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        String host = e.getMessage().getContentRaw().split("\\s+")[1];
        ChannelPool.joinPool(host, e.getChannel().getId());
        e.getMessage().reply(Callerphone.Callerphone + "Successfully joined channel pool hosted by " + Callerphone.jda.getTextChannelById(host).getName() + "*(" + host + ")*!").queue();
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "joinpool` - Join a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "join,joinpool,addpool".split(",");
    }
}
