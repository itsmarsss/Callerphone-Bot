package com.marsss.callerphone.msginbottle.handlers;

import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class SaveHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        ToolSet.sendPrivateEmbed(e.getUser(), e.getMessage().getEmbeds().get(0));

        e.reply(ToolSet.CP_EMJ + "A copy of this message bottle has been sent to your Direct Message.").setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "sve";
    }
}
