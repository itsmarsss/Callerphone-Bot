package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.IStringSelectInteraction;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class BottleSelectHandler implements IStringSelectInteraction {
    @Override
    public void runSelect(StringSelectInteractionEvent e) {
        BottleComponentIds.Parsed parsed = BottleComponentIds.parse(e.getComponentId());
        if (parsed == null || e.getValues().isEmpty()) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        String bottleId = e.getValues().get(0);
        Bottle bottle = MIB.getBottle(bottleId);
        if (bottle == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        MessageCreateData message = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (message == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        // Mark bottle-related inbox entries read when opening from Match inbox path may also apply
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().inbox()
                        .markReadBySource(e.getUser().getId(), bottleId);
            }
        } catch (Exception ignored) {
        }
        e.reply(message).setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return BottleComponentIds.PREFIX;
    }
}
