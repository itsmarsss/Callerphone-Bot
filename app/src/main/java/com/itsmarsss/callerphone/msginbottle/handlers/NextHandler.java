package com.itsmarsss.callerphone.msginbottle.handlers;

import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.IButtonInteraction;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class NextHandler implements IButtonInteraction {

    @Override
    public void runClick(ButtonInteraction e) {
        e.deferEdit().queue();

        String[] previousData = e.getButton().getCustomId().split("-");
        String id = previousData[1];
        int page = Integer.parseInt(previousData[2]);

        Bottle mib = MIB.getBottle(id);

        e.getHook().editOriginal(MessageEditData.fromCreateData(MessageInBottle.createMessage(mib, page))).queue();
    }

    @Override
    public String getID() {
        return "nxp";
    }
}
