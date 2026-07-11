package com.itsmarsss.callerphone.tccallerphone.handlers;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

/** Legacy reportchat-* buttons; new buttons use c-v1-report-*. */
public class ReportChatHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        String[] reportData = e.getButton().getCustomId().split("-", 2);
        if (reportData.length < 2) {
            e.reply(ToolSet.CP_EMJ + "Error reporting. Use `/reportcall` during a call.")
                    .setEphemeral(true).queue();
            return;
        }
        CallSessionService.get().reportById(reportData[1]);
        e.editButton(Button.danger("reportchat", "Chat Reported").asDisabled()).queue();
        e.getMessage().reply(ToolSet.CP_EMJ + " Call reported.").queue();
    }

    @Override
    public String getID() {
        return "reportchat";
    }
}
