package com.itsmarsss.callerphone.msginbottle.handlers;

import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import com.itsmarsss.commandType.IButtonInteraction;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public abstract class PaginationHandler implements IButtonInteraction {

    @Override
    public void runClick(ButtonInteraction e) {
        e.deferEdit().queue();

        String[] previousData = InteractionUtils.parseCustomId(e.getButton().getCustomId());
        if (previousData.length < 3) {
            return;
        }

        String id = previousData[1];
        int page;
        try {
            page = Integer.parseInt(previousData[2]);
        } catch (NumberFormatException ex) {
            return;
        }

        Bottle mib = MIB.getBottle(id);
        if (mib == null) {
            return;
        }

        MessageCreateData message = MessageInBottle.createMessage(mib, page);
        if (message == null) {
            return;
        }

        e.getHook().editOriginal(MessageEditData.fromCreateData(message)).queue();
    }
}
