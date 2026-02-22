package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnButtonClick extends ListenerAdapter {
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            String id = event.getButton().getCustomId().split("-")[0];
            if (Callerphone.btnMap.containsKey(id)) {
                Callerphone.btnMap.get(id).runClick(event);
                return;
            }
            event.reply(
                    ToolSet.CP_EMJ
                            + "Hmmm, the button `"
                            + event.getButton().getCustomId()
                            + "` shouldn't exist! Please join our support server and report this issue. "
                            + Callerphone.config.getSupportServer()
            ).queue();
        } catch (Exception e) {
            e.printStackTrace();
            sendError(event, e);
        }
    }

    public static void sendError(ButtonInteractionEvent event, Exception error) {
        event.reply(String.format(Response.ERROR_MSG.toString(), error.toString())).queue();
    }
}
