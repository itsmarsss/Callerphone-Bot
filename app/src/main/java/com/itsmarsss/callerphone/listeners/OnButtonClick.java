package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnButtonClick extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            String id = InteractionUtils.parseCustomId(event.getButton().getCustomId())[0];
            var handler = Callerphone.btnMap.get(id);
            if (handler != null) {
                handler.runClick(event);
                return;
            }
            event.reply(com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                    com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                    com.itsmarsss.callerphone.experience.ExperienceIntent.WARNING)
                            .title("That action expired")
                            .description("Open a fresh screen to continue. If this keeps happening, join support: "
                                    + Callerphone.config.getSupportServer())
                            .build()
            )).setEphemeral(true).queue();
        } catch (Exception e) {
            ErrorHandler.handleButtonError(event, e);
        }
    }
}
