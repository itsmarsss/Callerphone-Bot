package com.marsss.callerphone.channelpool.modals;

import com.marsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class SettingsModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {

    }

    @Override
    public String getID() {
        return "poolConfig";
    }
}
