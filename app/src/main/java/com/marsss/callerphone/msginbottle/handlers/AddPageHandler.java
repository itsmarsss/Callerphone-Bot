package com.marsss.callerphone.msginbottle.handlers;

import com.marsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.modals.Modal;

public class AddPageHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        String id = e.getButton().getCustomId().split("-")[1];

        TextInput message = TextInput.create("message", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Write your message in a bottle here")
                .setMinLength(10)
                .setMaxLength(1500)
                .build();

        TextInput signed = TextInput.create("signed", TextInputStyle.SHORT)
                .setPlaceholder("Set page signed here")
                .setMinLength(4)
                .setMaxLength(5)
                .setValue("true")
                .build();

        Modal modal = Modal.create("sendMIB-" + id, "Add Page To Message In Bottle")
                .addComponents(Label.of("Message (10 - 1500 characters)", message), Label.of("Page Signed (true | false)", signed))
                .build();

        e.replyModal(modal).queue();
    }

    @Override
    public String getID() {
        return "adp";
    }
}
