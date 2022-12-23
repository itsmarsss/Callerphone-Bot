package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

public class PoolParticipants implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().reply(poolParticipants(e.getChannel().getId())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolParticipants(e.getChannel().getId())).queue();
    }

    private final String CP_EMJ = Callerphone.Callerphone;

    private String poolParticipants(String id) {
        final ArrayList<String> PARTICIPANTS = ChannelPool.getClients(id);
        if (PARTICIPANTS.size() == 0) {
            return CP_EMJ + "This channel is not in a pool.";
        }
        final StringBuilder LIST = new StringBuilder();
        for (int i = 0; i < PARTICIPANTS.size(); i++) {
            LIST.append("\n`ID: ")
                    .append(PARTICIPANTS.get(i))
                    .append("` (#");

            final TextChannel TEXT_CHANNEL = Callerphone.getTextChannel(PARTICIPANTS.get(i));
            if(TEXT_CHANNEL == null) {
                LIST.append("#[N/A NOT FOUND])");
            } else {
                LIST.append(TEXT_CHANNEL.getName())
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
        return "`" + Callerphone.Prefix + "poolparts` - Show channel pool participants.";
    }

    @Override
    public String[] getTriggers() {
        return "poolparticipants,participants,parts,poolparts".split(",");
    }
}
