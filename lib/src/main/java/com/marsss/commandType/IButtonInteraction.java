package com.marsss.commandType;

import com.marsss.ICommand;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public interface IButtonInteraction {
    void runClick(ButtonInteraction e);

    String getID();
}
