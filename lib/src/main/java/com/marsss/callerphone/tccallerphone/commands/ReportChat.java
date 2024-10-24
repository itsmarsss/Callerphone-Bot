package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import com.marsss.callerphone.tccallerphone.ConvoStorage;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ReportChat implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(reportChat(e.getChannel())).queue();
    }

    private String reportChat(MessageChannelUnion channel) {
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
        return "</reportchat:1075168978189692948> - Report current chat with another server.";
    }

    @Override
    public String[] getTriggers() {
        return "reportchat,reportcall,reportphone,reportcallerphone,reportuserphone,reportphonecall".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
