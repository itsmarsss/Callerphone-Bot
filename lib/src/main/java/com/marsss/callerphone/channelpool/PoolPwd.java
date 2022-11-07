package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolPwd implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final String pwd = e.getMessage().getContentRaw().split("\\s+")[1];
        e.getMessage().reply(poolPwd(e.getChannel().getId(), pwd)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolPwd(e.getChannel().getId(), e.getOption("password").getAsString())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "poolpwd <password | \"none\" for no password>` - Set channel pool password.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "poolpwd <password | \"none\" for no password>` - Set channel pool password.";
    }

    @Override
    public String[] getTriggers() {
        return "password,pass,pwd,poolpass,poolpwd,poolpassword".split(",");
    }

    private String poolPwd(String id, String pwd) {
        if(pwd.equals("none"))
            pwd = "";

        int stat = ChannelPool.setPassword(id, pwd);
        if (stat == 202) {
            return Callerphone.Callerphone + "This pool now has password ||" + pwd + "||.";
        } else if (stat == 404) {
            return Callerphone.Callerphone + "This pool is not hosting a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
