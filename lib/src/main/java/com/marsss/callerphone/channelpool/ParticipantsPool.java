package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;

public class ParticipantsPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        ArrayList<String> participants = ChannelPool.getClients(e.getChannel().getId());
        if (participants.size() == 0) {
            MESSAGE.reply("This channel is not in a pool.").queue();
        } else {
            String list = "";
            for (int i = 0; i < participants.size(); i++) {
                list += "\n`#" + Callerphone.jda.getTextChannelById(participants.get(i)).getName() + "` (" + participants.get(i) + ")";
                if (i == 0) {
                    list += " [Host]";
                } else {
                    list += " [Client]";
                }
            }
            MESSAGE.replyEmbeds(new EmbedBuilder()
                    .setColor(new Color(114, 137, 218))
                    .setTitle("Participants of channel pool hosted by: `#" + Callerphone.jda.getTextChannelById(participants.get(0)).getName() + "` (" + participants.get(0) + ")")
                    .setDescription(list)
                    .build()
            ).queue();
        }
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

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
}
