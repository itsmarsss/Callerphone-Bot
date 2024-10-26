package com.marsss.callerphone.msginbottle.handlers;

import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.callerphone.msginbottle.entities.Bottle;
import com.marsss.commandType.IButtonInteraction;
import com.marsss.database.categories.MIB;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class PreviousHandler implements IButtonInteraction {

    @Override
    public void runClick(ButtonInteraction e) {
        e.deferEdit().queue();

        String[] previousData = e.getButton().getId().split("-");
        String uuid = previousData[1];
        int page = Integer.parseInt(previousData[2]);

        Bottle mib = MIB.getBottle(uuid);

        e.getHook().editOriginal(MessageEditData.fromCreateData(MessageInBottle.createMessage(mib, page))).queue();
    }

    @Override
    public String getID() {
        return "pvp";
    }
}
