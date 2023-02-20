package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PoolPub implements ICommand {

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

        final boolean PUB = Boolean.parseBoolean(args[1]);

        e.getMessage().reply(poolPub(e.getChannel().getId(), PUB)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final Member MEMBER = e.getMember();

        if (ChannelPool.permissionCheck(MEMBER, e)) {
            return;
        }

        e.reply(poolPub(e.getChannel().getId(), e.getOption("public").getAsBoolean())).queue();
    }

    private String poolPub(String id, boolean pub) {
        PoolStatus stat = ChannelPool.setPublicity(id, pub);

        if (stat == PoolStatus.SUCCESS) {

            return String.format(PoolResponse.POOL_PUB.toString(), (pub ? "public" : "private"));

        } else if (stat == PoolStatus.NOT_FOUND) {

            return PoolResponse.NOT_HOSTING.toString();

        }

        return ToolSet.CP_ERR + Response.ERROR.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "poolpub <true|false>` - Set channel pool publicity.";
    }

    @Override
    public String[] getTriggers() {
        return "public,publicity,poolpub,poolpublic,poolpublicity".split(",");
    }
}
