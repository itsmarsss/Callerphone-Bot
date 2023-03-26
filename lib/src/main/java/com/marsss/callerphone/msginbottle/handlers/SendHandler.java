package com.marsss.callerphone.msginbottle.handlers;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.MIBStatus;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SendHandler extends ListenerAdapter {
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent e) {
        if(e.getModalId().equals("sendMIB")) {

            MIBStatus stat = MessageInBottle.sendBottle(e.getUser().getId(), e.getValue("message").getAsString());

            switch(stat) {
                case RATE_LIMITED:
                    e.reply(ToolSet.CP_ERR + "You have reached your max send limit.").setEphemeral(true).queue();
                    break;
                case SENT:
                    e.reply(ToolSet.CP_EMJ + "Your bottle has been sent!").setEphemeral(true).queue();
                    break;
            }
        }
    }
}
