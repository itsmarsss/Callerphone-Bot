package com.marsss.callerphone.channelpool.commands;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolPub implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final boolean pub = Boolean.parseBoolean(e.getMessage().getContentRaw().split("\\s+")[1]);
        e.getMessage().reply(poolPub(e.getChannel().getId(), pub)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolPub(e.getChannel().getId(), e.getOption("publicity").getAsBoolean())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "poolpub <true|false>` - Set channel pool publicity.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "poolpub <true|false>` - Set channel pool publicity.";
    }

    @Override
    public String[] getTriggers() {
        return "public,publicity,poolpub,poolpublic,poolpublicity".split(",");
    }

    private String poolPub(String id, boolean pub) {
        int stat = ChannelPool.setPublicity(id, pub);
        if (stat == 202) {
            return Callerphone.Callerphone + "This pool is now " + (pub ? "public" : "private") + ".";
        } else if (stat == 404) {
            return Callerphone.Callerphone + "This pool is not hosting a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
