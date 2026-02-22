package com.marsss.commandType;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public interface IModalInteraction {
    void runModal(ModalInteractionEvent e);
    String getID();
}
