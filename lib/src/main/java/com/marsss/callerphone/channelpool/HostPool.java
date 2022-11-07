package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HostPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        int stat = ChannelPool.hostPool(e.getChannel().getId());
        if (stat == 413) {
            MESSAGE.reply(Callerphone.Callerphone + "This channel is already hosting a pool.").queue(m -> {
                m.editMessage(m.getContentRaw() + "\n`This channel's pool ID is: " + e.getChannel().getId() + "`" +
                        "\nSet a password with: `" + Callerphone.Prefix + "pwdpool <password>`").queue();
            });
        } else if (stat == 409) {
            MESSAGE.reply(Callerphone.Callerphone + "This channel is already in a pool.").queue();
        } else if (stat == 201) {
            MESSAGE.reply(Callerphone.Callerphone + "Successfully hosted channel pool for " + e.getChannel().getAsMention() + "!").queue(m -> {
                m.editMessage(m.getContentRaw() + "\n`This channel's pool ID is: " + e.getChannel().getId() + "`" +
                        "\nSet a password with: `" + Callerphone.Prefix + "poolpass <password>`").queue();
            });
        }
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "hostpool` - Host a channel pool.";
    }

    @Override
    public String[] getTriggers() {
        return "host,hostpool,startpool".split(",");
    }
}
