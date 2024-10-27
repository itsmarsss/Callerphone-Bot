package com.marsss.callerphone.msginbottle.modals;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.msginbottle.MIBResponse;
import com.marsss.callerphone.msginbottle.MIBStatus;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.IModalInteraction;
import com.marsss.database.categories.Cooldown;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class SendModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        String[] sendData = e.getModalId().split("-");

        String uuid = sendData.length > 1 ? sendData[1] : null;

        String message = e.getValue("message").getAsString();
        String messageFiltered = ToolSet.messageCheck(message);
        messageFiltered = ToolSet.filter(messageFiltered);

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

        MIBStatus stat = MessageInBottle.sendBottle(e.getUser().getId(), message, signed, uuid);

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
