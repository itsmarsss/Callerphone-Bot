package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.ConvoStorage;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ReportChat implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().reply(reportChat(e.getChannel())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    private String reportChat(TextChannel channel) {
        if (!TCCallerphone.hasCall(channel.getId())) {
            return Callerphone.Callerphone + "There isn't a chat going on!";
        }
        ConvoStorage call = TCCallerphone.getCall(channel.getId());
        if (call != null) {
            call.setReport(true);
            return Callerphone.Callerphone + "Chat reported!";
        }
        return Callerphone.Callerphone + "Something went wrong, couldn't report call.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "report,reportchat,reportcall,reportphone,reportcallerphone,reportuserphone,reportphonecall".split(",");
    }
}
