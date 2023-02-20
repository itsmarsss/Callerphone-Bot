package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class JoinPool implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e.getMessage())) {
            return;
        }

        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1) {
            e.getMessage().reply(String.format(ToolSet.CP_ERR + Response.MISSING_PARAM.toString(), Callerphone.config.getPrefix())).queue();
            return;
        }

        final String host = e.getMessage().getContentRaw().split("\\s+")[1];

        String pwd = "";

        if (args.length >= 3)
            pwd = args[2];

        e.getMessage().reply(joinPool(e.getChannel(), host, pwd)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        List<OptionMapping> param = e.getOptions();

        if (param.size() == 1) {
            e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), "")).queue();
            return;
        }

        e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), e.getOption("password").getAsString())).queue();
    }

    private String joinPool(MessageChannel channel, String host, String pwd) {
        PoolStatus stat = ChannelPool.joinPool(host, channel.getId(), pwd);

        final String id = channel.getId();

        final TextChannel HOST_CHANNEL = ToolSet.getTextChannel(host);

        if (HOST_CHANNEL == null) {
            ChannelPool.clearChildren(host);
            return ToolSet.CP_EMJ + PoolResponse.REQUESTED_ID_NOT_FOUND.toString();
        }

        if (stat == PoolStatus.IS_HOST) {

            return ToolSet.CP_EMJ + PoolResponse.ALREADY_HOSTING +
                    String.format(PoolResponse.POOL_ID.toString(), id) + "\n" +
                    (ChannelPool.hasPassword(id)
                            ? String.format(PoolResponse.POOL_PWD.toString(), ChannelPool.getPassword(id))
                            : String.format(PoolResponse.POOL_SET_PWD.toString(), Callerphone.config.getPrefix())) + "\n" +
                    String.format(PoolResponse.POOL_END_WITH.toString(), Callerphone.config.getPrefix());

        } else if (stat == PoolStatus.IS_CHILD) {

            return ToolSet.CP_EMJ + PoolResponse.ALREADY_IN_POOL + "\n" +
                    String.format(PoolResponse.POOL_LEAVE_WITH.toString(), Callerphone.config.getPrefix());

        } else if (stat == PoolStatus.NOT_FOUND) {

            return String.format(ToolSet.CP_EMJ + PoolResponse.REQUESTED_ID_NOT_FOUND.toString(), host);

        } else if (stat == PoolStatus.INCORRECT_PASS) {

            HOST_CHANNEL.sendMessage(String.format(ToolSet.CP_EMJ + PoolResponse.JOIN_INCORRECT_PWD.toString(), channel.getId())).queue();
            return String.format(ToolSet.CP_EMJ + PoolResponse.REQUESTED_ID_NOT_FOUND.toString(), host);

        } else if (stat == PoolStatus.FULL) {

            HOST_CHANNEL.sendMessage(String.format(String.format(ToolSet.CP_EMJ + PoolResponse.JOIN_FULL_POOL.toString(), channel.getId()))).queue();
            return ToolSet.CP_EMJ + PoolResponse.ALREADY_FULL.toString();

        } else if (stat == PoolStatus.SUCCESS) {

            return String.format(ToolSet.CP_EMJ + PoolResponse.JOIN_POOL_SUCCESS.toString(), host, HOST_CHANNEL.getName());

       }

        return ToolSet.CP_ERR + Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "joinpool <ID> <password>` - Join a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "join,joinpool,addpool".split(",");
    }
}
