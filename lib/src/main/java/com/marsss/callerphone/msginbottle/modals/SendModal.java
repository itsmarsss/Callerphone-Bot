package com.marsss.callerphone.msginbottle.modals;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.MIBResponse;
import com.marsss.callerphone.msginbottle.MIBStatus;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class SendModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        String message = e.getValue("message").getAsString();
        String messageFiltered = ToolSet.messageCheck(message);
        messageFiltered = ToolSet.filter(messageFiltered);

        if(!message.equals(messageFiltered)) {
            e.reply(MIBResponse.MESSAGE_FLAGGED.toString()).setEphemeral(true).queue();
            return;
        }

        MIBStatus stat = MessageInBottle.sendBottle(e.getUser().getId(), message);

        switch (stat) {
            case RATE_LIMITED:
                e.reply(MIBResponse.SEND_MAX.toString()).setEphemeral(true).queue();
                break;
            case SENT:
                e.reply(MIBResponse.SEND_SUCCESS.toString()).setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public String getID() {
        return "sendMIB";
    }
}
