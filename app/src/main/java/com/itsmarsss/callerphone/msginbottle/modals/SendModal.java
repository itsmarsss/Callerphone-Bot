package com.itsmarsss.callerphone.msginbottle.modals;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.msginbottle.MIBResponse;
import com.itsmarsss.callerphone.msginbottle.MIBStatus;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.commandType.IModalInteraction;
import com.itsmarsss.database.categories.Cooldown;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class SendModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        String[] sendData = e.getModalId().split("-");

        String id = sendData.length > 1 ? sendData[1] : null;

        String message = e.getValue("message").getAsString();
        String messageFiltered = ToolSet.filterMessage(message);

        if (!message.equals(messageFiltered)) {
            e.reply(MIBResponse.MESSAGE_FLAGGED.toString()).setEphemeral(true).queue();
            return;
        }

        String signedStr = e.getValue("signed").getAsString().toLowerCase();
        boolean signed;

        if (signedStr.equals("true")) {
            signed = true;
        } else if (signedStr.equals("false")) {
            signed = false;
        } else {
            e.reply(MIBResponse.INVALID_SIGNED.toString()).setEphemeral(true).queue();
            return;
        }

        MIBStatus stat = MessageInBottle.sendBottle(e.getUser().getId(), messageFiltered, signed, id);

        switch (stat) {
            case RATE_LIMITED:
                e.reply(MIBResponse.SEND_MAX.toString()).setEphemeral(true).queue();
                break;
            case ERROR:
                e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
                break;
            case SENT:
                Cooldown.setMIBSendCoolDown(e.getUser().getId());
                e.reply(MIBResponse.SEND_SUCCESS.toString()).setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public String getID() {
        return "sendMIB";
    }
}
