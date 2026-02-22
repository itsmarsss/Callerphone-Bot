package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnButtonClick extends ListenerAdapter {
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            String id = InteractionUtils.parseCustomId(event.getButton().getCustomId())[0];
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
            ErrorHandler.handleButtonError(event, e);
        }
    }
}
