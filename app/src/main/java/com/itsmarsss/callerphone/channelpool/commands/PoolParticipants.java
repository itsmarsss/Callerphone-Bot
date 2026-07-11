package com.itsmarsss.callerphone.channelpool.commands;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.channelpool.ChannelPool;
import com.itsmarsss.callerphone.channelpool.PoolResponse;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public class PoolParticipants implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(poolParticipants(e.getChannel().getId())).setEphemeral(true).queue();
    }

    private String poolParticipants(String id) {
        List<String> participants = ChannelPool.getClients(id);
        if (participants.isEmpty()) {
            return PoolResponse.NOT_IN_POOL.toString();
        }

        StringBuilder list = new StringBuilder();
        for (int i = 0; i < participants.size(); i++) {
            String channelId = participants.get(i);
            list.append("\n`ID: ").append(channelId).append("` (");

            TextChannel channel = ToolSet.getTextChannel(channelId);
            if (channel == null) {
                list.append("[N/A NOT FOUND] | #[N/A NOT FOUND])");
            } else {
                list.append(channel.getGuild().getName())
                        .append(" | #")
                        .append(channel.getName())
                        .append(")");
            }

            list.append(i == 0 ? " [Host] :crown:" : " [Client] :link:");
        }
        return list.toString();
    }

    @Override
    public String getHelp() {
        return "`/poolparts` - Show channel pool participants.";
    }

    @Override
    public String getName() {
        return "poolparts";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Show channel pool participants")
                .setContexts(InteractionContextType.GUILD);
    }
}
