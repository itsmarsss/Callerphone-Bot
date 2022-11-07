package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HostPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        ChannelPool.hostPool(e.getChannel().getId());
        e.getMessage().reply(Callerphone.Callerphone + "Successfully hosted channel pool for " + e.getChannel().getAsMention() + "!").queue();
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "Help Here";
    }

    @Override
    public String[] getTriggers() {
        return "hostpool,startpool".split(",");
    }
}
