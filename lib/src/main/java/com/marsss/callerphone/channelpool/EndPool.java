package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class EndPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        int stat = ChannelPool.endPool(e.getChannel().getId());
        if (stat == 404) {
            MESSAGE.reply(Callerphone.Callerphone + "This channel is not hosting a pool.").queue();
        } else if (stat == 200) {
            e.getMessage().reply(Callerphone.Callerphone + "Successfully ended channel pool!").queue();
        }
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "endpool` - End a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "end,endpool,stoppool".split(",");
    }
}
