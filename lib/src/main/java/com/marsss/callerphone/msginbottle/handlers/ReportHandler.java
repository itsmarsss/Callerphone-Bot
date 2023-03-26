package com.marsss.callerphone.msginbottle.handlers;

import com.marsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class ReportHandler implements IButtonInteraction {

    @Override
    public void runClick(ButtonInteraction e) {

    }

    @Override
    public String getID() {
        return "rpt";
    }
}
