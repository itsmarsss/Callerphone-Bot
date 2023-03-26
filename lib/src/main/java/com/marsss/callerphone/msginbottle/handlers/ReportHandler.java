package com.marsss.callerphone.msginbottle.handlers;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class ReportHandler implements IButtonInteraction {

    @Override
    public void runClick(ButtonInteraction e) {
        final TextChannel REPORT_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getReportChatChannel());
        REPORT_CHANNEL.sendMessageEmbeds(e.getMessage().getEmbeds()).queue();
        e.reply(ToolSet.CP_EMJ + "Reported!").setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "rpt";
    }
}
