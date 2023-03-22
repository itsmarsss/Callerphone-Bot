package com.marsss.commandType;

import com.marsss.ICommand;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public interface IButtonInteraction extends ICommand {
    void runClick(ButtonInteraction e);
}
