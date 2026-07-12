package com.itsmarsss.commandType;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public interface IStringSelectInteraction {
    void runSelect(StringSelectInteractionEvent e);

    /** First segment of custom id used for routing (e.g. {@code m}). */
    String getID();
}
