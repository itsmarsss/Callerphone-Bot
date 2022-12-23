package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolPwd implements ICommand {

    private final String CP_EMJ = Callerphone.Callerphone;

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Member MEMBER = e.getMember();
        if (MEMBER == null) {
            return;
        }

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(CP_EMJ + "Missing parameters, do `" + Callerphone.Prefix + "help poolpwd` for more information.").queue();
            return;
        }

        final String PWD = args[1];

        e.getMessage().reply(poolPwd(e.getChannel().getId(), PWD)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final Member MEMBER = e.getMember();
        if (MEMBER == null) {
            return;
        }

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(poolPwd(e.getChannel().getId(), e.getOption("password").getAsString())).setEphemeral(true).queue();
    }

    private String poolPwd(String id, String pwd) {
        if (pwd.equals("none"))
            pwd = "";

        PoolStatus stat = ChannelPool.setPassword(id, pwd);
        if (stat == PoolStatus.SUCCESS) {
            return CP_EMJ + "This pool now has " +
                    ((pwd.equals("")) ? "no password" : "password ||" + pwd + "||") +
                    ".";
        } else if (stat == PoolStatus.ERROR) {
            return CP_EMJ + "This pool is not hosting a pool.";
        }
        return CP_EMJ + "An error occurred.";
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "poolpwd <password | \"none\" for no password>` - Set channel pool password.";
    }

    @Override
    public String[] getTriggers() {
        return "password,pass,pwd,poolpass,poolpwd,poolpassword".split(",");
    }
}
