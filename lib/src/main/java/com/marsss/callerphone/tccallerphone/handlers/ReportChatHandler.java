package com.marsss.callerphone.tccallerphone.handlers;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import com.marsss.callerphone.tccallerphone.entities.ConversationStorage;
import com.marsss.commandType.IButtonInteraction;
import com.marsss.database.categories.Chats;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class ReportChatHandler implements IButtonInteraction {

    @Override
    public void runClick(ButtonInteraction e) {
        String[] reportData = e.getButton().getId().split("-");
        String id = reportData[1];

        ConversationStorage convo = Chats.queryChat(id);

        if(convo == null) {
            e.reply(ToolSet.CP_EMJ + "Error with reporting, you can also report during a chat with </reportchat:1075168978189692948>!").setEphemeral(true).queue();
            return;
        }

        TCCallerphone.report(convo);

        e.editButton(Button.danger("reportchat", "Chat Reported").asDisabled()).queue();
        e.getMessage().reply(ToolSet.CP_EMJ + "Chat Reported, you can also report during a chat with </reportchat:1075168978189692948>!").queue();
    }

    @Override
    public String getID() {
        return "reportchat";
    }
}
