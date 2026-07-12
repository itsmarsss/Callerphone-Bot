package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnStringSelect extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        try {
            String id = InteractionUtils.parseCustomId(event.getComponentId())[0];
            var handler = Callerphone.selMap.get(id);
            if (handler != null) {
                handler.runSelect(event);
                return;
            }
            event.reply(
                    ToolSet.CP_EMJ
                            + " That menu expired. Open a fresh screen to continue."
            ).setEphemeral(true).queue();
        } catch (Exception e) {
            ErrorHandler.handleSelectError(event, e);
        }
    }
}
