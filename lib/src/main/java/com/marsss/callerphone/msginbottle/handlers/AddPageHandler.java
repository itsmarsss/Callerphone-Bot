package com.marsss.callerphone.msginbottle.handlers;

import com.marsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class AddPageHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        String uuid = e.getButton().getId().split("-")[1];

        TextInput message = TextInput.create("message", "Message (10 - 1500 characters)", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Write your message in a bottle here")
                .setMinLength(10)
                .setMaxLength(1500)
                .build();

        TextInput signed = TextInput.create("signed", "Page Signed (true | false)", TextInputStyle.SHORT)
                .setPlaceholder("Set page signed here")
                .setMinLength(4)
                .setMaxLength(5)
                .setValue("true")
                .build();

        Modal modal = Modal.create("sendMIB-" + uuid, "Add Page To Message In Bottle")
                .addComponents(ActionRow.of(message), ActionRow.of(signed))
                .build();

        e.replyModal(modal).queue();
    }

    @Override
    public String getID() {
        return "adp";
    }
}
