package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class JoinPool implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        List<OptionMapping> param = e.getOptions();

        if (param.size() == 1) {
            e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), "")).setEphemeral(true).queue();
            return;
        }

        e.reply(joinPool(e.getChannel(), e.getOption("hostid").getAsString(), e.getOption("password").getAsString())).setEphemeral(true).queue();
    }

    private String joinPool(MessageChannelUnion channel, String host, String pwd) {
        PoolStatus stat = ChannelPool.joinPool(host, channel.getId(), pwd);

        final String id = channel.getId();

        final TextChannel HOST_CHANNEL = ToolSet.getTextChannel(host);

        if (HOST_CHANNEL == null) {
            ChannelPool.clearChildren(host);
            return String.format(PoolResponse.REQUESTED_ID_NOT_FOUND.toString(), host);
        }

        if (stat == PoolStatus.IS_HOST) {

            return PoolResponse.ALREADY_HOSTING +
                    String.format(PoolResponse.POOL_ID.toString(), id) + "\n" +
                    (ChannelPool.hasPassword(id)
                            ? String.format(PoolResponse.POOL_PWD.toString(), ChannelPool.getPassword(id))
                            : PoolResponse.POOL_SET_SETTINGS) + "\n" +
                    PoolResponse.POOL_END_WITH;

        } else if (stat == PoolStatus.IS_CHILD) {

            return PoolResponse.ALREADY_IN_POOL + "\n" + PoolResponse.POOL_LEAVE_WITH;

        } else if (stat == PoolStatus.NOT_FOUND) {

            return String.format(PoolResponse.REQUESTED_ID_NOT_FOUND.toString(), host);

        } else if (stat == PoolStatus.INCORRECT_PASS) {

            HOST_CHANNEL.sendMessage(String.format(PoolResponse.JOIN_INCORRECT_PWD.toString(), channel.getId())).queue();
            return String.format(PoolResponse.REQUESTED_ID_NOT_FOUND.toString(), host);

        } else if (stat == PoolStatus.FULL) {

            HOST_CHANNEL.sendMessage(String.format(String.format(PoolResponse.JOIN_FULL_POOL.toString(), channel.getId()))).queue();
            return PoolResponse.ALREADY_FULL.toString();

        } else if (stat == PoolStatus.SUCCESS) {

            return String.format(PoolResponse.JOIN_POOL_SUCCESS.toString(), host, HOST_CHANNEL.getName());

       }

        return Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`/joinpool <ID> <password>` - Join a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "join,joinpool,addpool".split(",");
    }
}
