package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LeavePool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.getMessage().reply("You need `Manage Channel` permission to run this command.").queue();
            return;
        }

        final Message MESSAGE = e.getMessage();
        int stat = ChannelPool.leavePool(e.getChannel().getId());
        if (stat == 404) {
            MESSAGE.reply(Callerphone.Callerphone + "This channel is not in a pool.").queue();
        } else if (stat == 409) {
            MESSAGE.reply(Callerphone.Callerphone + "This channel is hosting a pool.").queue();
        } else if (stat == 200) {
            e.getMessage().reply(Callerphone.Callerphone + "Successfully left channel pool!").queue();
        }
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "leavepool` - Leave a channel pool.";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "leavepool` - Leave a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "leave,leavepool,exitpool".split(",");
    }
}
