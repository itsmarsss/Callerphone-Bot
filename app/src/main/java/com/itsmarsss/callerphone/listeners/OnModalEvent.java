package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnModalEvent extends ListenerAdapter {
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        try {
            String id = InteractionUtils.parseCustomId(event.getModalId())[0];
            var handler = Callerphone.mdlMap.get(id);
            if (handler != null) {
                handler.runModal(event);
                return;
            }
            event.reply(com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                    com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                    com.itsmarsss.callerphone.experience.ExperienceIntent.WARNING)
                            .title("That form expired")
                            .description("Open a fresh screen and try again. If this keeps happening, join support: "
                                    + Callerphone.config.getSupportServer())
                            .build()
            )).setEphemeral(true).queue();
        } catch (Exception e) {
            ErrorHandler.handleModalError(event, e);
        }
    }
}
