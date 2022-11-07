package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

public class ParticipantsPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().reply(poolParticipants(e.getChannel().getId())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.reply(poolParticipants(e.getChannel().getId())).queue();
    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "poolparts` - Show channel pool participants.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "poolparts` - Show channel pool participants.";
    }

    @Override
    public String[] getTriggers() {
        return "poolparticipants,participants,parts,poolparts".split(",");
    }

    private String poolParticipants(String id) {
        final ArrayList<String> participants = ChannelPool.getClients(id);
        if (participants.size() == 0) {
            return "This channel is not in a pool.";
        } else {
            StringBuilder list = new StringBuilder();
            for (int i = 0; i < participants.size(); i++) {
                list.append("\n`#").append(Callerphone.jda.getTextChannelById(participants.get(i)).getName()).append("` (").append(participants.get(i)).append(")");
                if (i == 0) {
                    list.append(" [Host]");
                } else {
                    list.append(" [Client]");
                }
            }
            return list.toString();
        }
    }
}
