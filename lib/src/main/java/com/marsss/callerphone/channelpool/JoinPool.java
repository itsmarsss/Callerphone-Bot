package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class JoinPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.getMessage().reply("You need `Manage Channel` permission to run this command.").queue();
            return;
        }

        final Message MESSAGE = e.getMessage();
        String host = e.getMessage().getContentRaw().split("\\s+")[1];
        int stat = ChannelPool.joinPool(host, e.getChannel().getId());
        if (stat == 413) {
            MESSAGE.reply(Callerphone.Callerphone + "This channel is hosting a pool.").queue(m -> {
                m.editMessage(m.getContentRaw() + "\n`This channel's pool ID is: " + e.getChannel().getId() + "`" +
                        "\nEnd pool with: `" + Callerphone.Prefix + "endpool`").queue();
            });
        } else if (stat == 414) {
            MESSAGE.reply(Callerphone.Callerphone + "This pool is already full 10/10.").queue();
            Callerphone.jda.getTextChannelById(host).sendMessage("Channel ID: " + MESSAGE.getChannel().getId() + " attempted to join a full pool *(this one)*.").queue();
        } else if (stat == 409) {
            MESSAGE.reply(Callerphone.Callerphone + "This channel is already in a pool.").queue(m -> {
                m.editMessage(m.getContentRaw() + "\nLeave pool with: `" + Callerphone.Prefix + "leavepool`").queue();
            });
        } else if (stat == 404) {
            MESSAGE.reply(Callerphone.Callerphone + "Requested pool ID *(" + host + ")* does not exist.").queue();
        } else if (stat == 200) {
            e.getMessage().reply(Callerphone.Callerphone + "Successfully joined channel pool hosted by `#" + Callerphone.jda.getTextChannelById(host).getName() + "`*(ID: " + host + ")*!").queue();
        }
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "joinpool` - Join a channel pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "joinpool` - Join a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "join,joinpool,addpool".split(",");
    }
}
