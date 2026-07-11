package com.itsmarsss.callerphone.tccallerphone.handlers;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.tccallerphone.services.ConversationService;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class ReportChatHandler implements IButtonInteraction {
    private final ConversationService conversationService = ConversationService.getInstance();

    @Override
    public void runClick(ButtonInteraction e) {
        String[] reportData = e.getButton().getCustomId().split("-");
        if (reportData.length < 2) {
            e.reply(ToolSet.CP_EMJ + "Error with reporting, you can also report during a chat with </reportchat:1075168978189692948>!")
                    .setEphemeral(true).queue();
            return;
        }

        String id = reportData[1];
        conversationService.reportById(id);

        e.editButton(Button.danger("reportchat", "Chat Reported").asDisabled()).queue();
        e.getMessage().reply(ToolSet.CP_EMJ + "Chat Reported, you can also report during a chat with </reportchat:1075168978189692948>!").queue();
    }

    @Override
    public String getID() {
        return "reportchat";
    }
}
