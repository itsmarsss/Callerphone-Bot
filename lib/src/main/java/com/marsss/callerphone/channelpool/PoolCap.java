package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolCap implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final int cap = Integer.parseInt(e.getMessage().getContentRaw().split("\\s+")[1]);
        e.getMessage().reply(poolCap(e.getChannel().getId(), cap)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolCap(e.getChannel().getId(), (int) e.getOption("capacity").getAsLong())).queue();
    }


    public static String getHelp() {
        return "`" + Callerphone.Prefix + "poolcap <number [1-10]>` - Set channel pool capacity";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "poolcap <number [1-10]>` - Set channel pool capacity";
    }

    @Override
    public String[] getTriggers() {
        return "cap,capcity,poolcap,poolcapacity".split(",");
    }

    private String poolCap(String id, int cap) {
        int stat = ChannelPool.setCap(id, cap);
        if(stat == 202){
            return "This pool now has capacity **" + cap + "**.";
        }
        return "An error occurred.";
    }
}
