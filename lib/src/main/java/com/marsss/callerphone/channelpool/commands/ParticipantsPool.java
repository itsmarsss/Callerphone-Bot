package com.marsss.callerphone.channelpool.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.listeners.CommandListener;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

public class ParticipantsPool implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        try {
            e.getMessage().reply(poolParticipants(e.getChannel().getId())).queue();
        } catch (Exception ex) {
            CommandListener.sendError(e.getMessage(), ex);
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolParticipants(e.getChannel().getId())).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "poolparts` - Show channel pool participants.";
    }

    @Override
    public String[] getTriggers() {
        return "poolparticipants,participants,parts,poolparts".split(",");
    }

    private String poolParticipants(String id) {
        final ArrayList<String> participants = ChannelPool.getClients(id);
        if (participants.size() == 0) {
            return Callerphone.Callerphone + "This channel is not in a pool.";
        }
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < participants.size(); i++) {
            list.append("\n`ID: ")
                    .append(participants.get(i))
                    .append("` (#")
                    .append(Callerphone.jda.getTextChannelById(participants.get(i)).getName())
                    .append(")");
            if (i == 0) {
                list.append(" [Host] :crown:");
            } else {
                list.append(" [Client] :link:");
            }
        }
        return list.toString();
    }
}
