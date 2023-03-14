package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import com.marsss.callerphone.tccallerphone.ConvoStorage;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class ReportChat implements ICommand {
    @Override
    public void runCommand(MessageReceivedEvent e) {
        e.getMessage().reply(reportChat(e.getChannel())).queue();
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(reportChat(e.getTextChannel())).queue();
    }

    private String reportChat(TextChannel channel) {
        if (!TCCallerphone.hasCall(channel.getId())) {
            return ChatResponse.CHAT_REPORT_VIA_SERVER.toString();
        }
        ConvoStorage call = TCCallerphone.getCall(channel.getId());
        if (call != null) {
            call.setReport(true);
            return ChatResponse.CHAT_REPORTED_SUCCESS.toString();
        }
        return ChatResponse.CHAT_REPORTED_NOT_SUCCESS.toString();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "report` - Report current chat with another server.";
    }

    @Override
    public String[] getTriggers() {
        return "report,reportchat,reportcall,reportphone,reportcallerphone,reportuserphone,reportphonecall".split(",");
    }
}
