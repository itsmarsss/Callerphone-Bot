package com.marsss.callerphone.msginbottle.modals;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.MIBStatus;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class SendModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        MIBStatus stat = MessageInBottle.sendBottle(e.getUser().getId(), e.getValue("message").getAsString());

        switch (stat) {
            case RATE_LIMITED:
                e.reply(ToolSet.CP_ERR + "You have reached your max send limit.").setEphemeral(true).queue();
                break;
            case SENT:
                e.reply(ToolSet.CP_EMJ + "Your bottle has been sent!").setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public String getID() {
        return "sendMIB";
    }
}
