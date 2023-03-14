package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class PoolPwd implements ICommand {

    @Override
    public void runCommand(MessageReceivedEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(Response.MISSING_PARAM.toString()).queue();
            return;
        }

        final String PWD = args[1];

        e.getMessage().reply(poolPwd(e.getChannel().getId(), PWD)).queue();
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(poolPwd(e.getChannel().getId(), e.getOption("password").getAsString())).setEphemeral(true).queue();
    }

    private String poolPwd(String id, String pwd) {
        if (pwd.equals("none"))
            pwd = "";

        if(pwd.contains(""))
            return Response.ERROR.toString();

        PoolStatus stat = ChannelPool.setPassword(id, pwd);

        if (stat == PoolStatus.SUCCESS) {

            return String.format(PoolResponse.POOL_PWD.toString(), (pwd.equals("") ? "**no password**" : "`||" + pwd + "||`"));

        } else if (stat == PoolStatus.NOT_FOUND) {

            return PoolResponse.NOT_HOSTING.toString();

        }

        return Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "poolpwd <password | \"none\" for no password>` - Set channel pool password.";
    }

    @Override
    public String[] getTriggers() {
        return "password,pass,pwd,poolpass,poolpwd,poolpassword".split(",");
    }
}
