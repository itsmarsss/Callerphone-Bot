package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnModalEvent extends ListenerAdapter {
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        try {
            String id = InteractionUtils.parseCustomId(event.getModalId())[0];
            if (Callerphone.mdlMap.containsKey(id)) {
                Callerphone.mdlMap.get(id).runModal(event);
                return;
            }
            event.reply(
                    ToolSet.CP_EMJ
                            + "Hmmm, the modal `"
                            + event.getModalId()
                            + "` shouldn't exist! Please join our support server and report this issue. "
                            + Callerphone.config.getSupportServer()
            ).queue();
        } catch (Exception e) {
            ErrorHandler.handleModalError(event, e);
        }
    }
}
