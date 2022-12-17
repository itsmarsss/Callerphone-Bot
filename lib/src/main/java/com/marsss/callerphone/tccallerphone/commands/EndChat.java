package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class EndChat implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().reply(endChat(e.getChannel())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    private String endChat(TextChannel channel) {
        if(!TCCallerphone.hasCall(channel.getId())) {
            return Callerphone.Callerphone + "There is not a call going on!";
        }
        return TCCallerphone.onEndCallCommand(channel);
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return new String[0];
    }
}
