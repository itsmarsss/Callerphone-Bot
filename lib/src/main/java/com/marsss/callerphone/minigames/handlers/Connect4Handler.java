package com.marsss.callerphone.minigames.handlers;

import com.marsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class Connect4Handler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {

    }

    @Override
    public String getID() {
        return "cn4";
    }
}
