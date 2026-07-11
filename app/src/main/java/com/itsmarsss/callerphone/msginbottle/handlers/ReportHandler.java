package com.itsmarsss.callerphone.msginbottle.handlers;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class ReportHandler implements IButtonInteraction {

    @Override
    public void runClick(ButtonInteraction e) {
        String[] reportData = e.getButton().getCustomId().split("-");
        if (reportData.length < 3) {
            e.reply(ToolSet.CP_EMJ + "Invalid report button.").setEphemeral(true).queue();
            return;
        }

        String id = reportData[1];
        String pageNum = reportData[2];

        TextChannel reportChannel = ToolSet.getTextChannel(Callerphone.config.getReportChatChannel());
        if (reportChannel == null) {
            e.reply(ToolSet.CP_EMJ + "Report channel is not configured.").setEphemeral(true).queue();
            return;
        }

        reportChannel.sendMessage("**ID**: " + id + "\n**Page**: " + pageNum)
                .addEmbeds(e.getMessage().getEmbeds())
                .queue();

        e.reply(ToolSet.CP_EMJ + "Reported!").setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "rpt";
    }
}
