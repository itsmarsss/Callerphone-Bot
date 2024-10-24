package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.LinkedList;

public class PoolParticipants implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(poolParticipants(e.getChannel().getId())).setEphemeral(true).queue();
    }

    private String poolParticipants(String id) {
        final LinkedList<String> PARTICIPANTS = ChannelPool.getClients(id);

        if (PARTICIPANTS.isEmpty()) {
            return PoolResponse.NOT_IN_POOL.toString();
        }

        final StringBuilder LIST = new StringBuilder();
        for (int i = 0; i < PARTICIPANTS.size(); i++) {
            LIST.append("\n`ID: ")
                    .append(PARTICIPANTS.get(i))
                    .append("` (");

            final TextChannel TEXT_CHANNEL = ToolSet.getTextChannel(PARTICIPANTS.get(i));
            if (TEXT_CHANNEL == null) {
                LIST.append("[N/A NOT FOUND] | #[N/A NOT FOUND])");
            } else {
                LIST.append(TEXT_CHANNEL.getGuild().getName()).append(" | #")
                        .append(TEXT_CHANNEL.getName())
                        .append(")");
            }
            if (i == 0) {
                LIST.append(" [Host] :crown:");
            } else {
                LIST.append(" [Client] :link:");
            }
        }

        return LIST.toString();
    }

    @Override
    public String getHelp() {
        return "`/poolparts` - Show channel pool participants.";
    }

    @Override
    public String[] getTriggers() {
        return "poolparts,poolparticipants,participants,parts".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
