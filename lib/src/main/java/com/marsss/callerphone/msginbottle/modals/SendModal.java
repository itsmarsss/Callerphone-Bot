package com.marsss.callerphone.msginbottle.modals;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.MIBStatus;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import javax.tools.Tool;

public class SendModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        String message = e.getValue("message").getAsString();
        message = ToolSet.messageCheck(message);
        message = ToolSet.filter(message);

        if(!message.equals(message)) {
            e.reply(ToolSet.CP_ERR + "Your message has been flagged. Please remove any links, pings, or inappropriate words.").setEphemeral(true).queue();
            return;
        }

        MIBStatus stat = MessageInBottle.sendBottle(e.getUser().getId(), message);

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
