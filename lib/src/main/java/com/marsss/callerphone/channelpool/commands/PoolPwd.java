package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolPwd implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {

        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.getMessage().reply(Callerphone.Callerphone + "You need `Manage Channel` permission to run this command.").queue();
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Callerphone.Callerphone + "Missing parameters, do `" + Callerphone.Prefix + "help poolpwd` for more information.").queue();
            return;
        }

        final String pwd = args[1];

        try {
            e.getMessage().reply(poolPwd(e.getChannel().getId(), pwd)).queue();
        } catch (Exception ex) {
            CommandListener.sendError(e.getMessage(), ex);
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolPwd(e.getChannel().getId(), e.getOption("password").getAsString())).queue();
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
        if (pwd.equals("none"))
            pwd = "";

        int stat = ChannelPool.setPassword(id, pwd);
        if (stat == ChannelPool.SUCCESS) {
            if (pwd.equals("")) {
                return Callerphone.Callerphone + "This pool now has no password.";
            } else {
                return Callerphone.Callerphone + "This pool now has password ||" + pwd + "||.";
            }

        } else if (stat == ChannelPool.ERROR) {
            return Callerphone.Callerphone + "This pool is not hosting a pool.";
        }
        return Callerphone.Callerphone + "An error occurred.";
    }
}
