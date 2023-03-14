package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PoolPub implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
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

        return Response.ERROR.toString();
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
