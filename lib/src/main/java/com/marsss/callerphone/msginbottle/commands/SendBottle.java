package com.marsss.callerphone.msginbottle.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.MIBStatus;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class SendBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

        TextInput message = TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Write your message in a bottle here")
                .setMinLength(10)
                .setMaxLength(1500)
                .build();

        Modal modal = Modal.create("sendMIB", "Send Message In Bottle")
                .addComponents(ActionRow.of(message))
                .build();

        e.replyModal(modal).queue();
    }


    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "sendbottle` - Send a message in bottle into the seas for someone to read.";
    }

    @Override
    public String[] getTriggers() {
        return "sendbottle,mibsend".split(",");
    }
}
