package com.marsss.callerphone.msginbottle.commands;

import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class SendBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
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


        Modal modal = Modal.create("sendMIB", "Send Message In Bottle")
                .addComponents(ActionRow.of(message), ActionRow.of(signed))
                .build();

        e.replyModal(modal).queue();
    }


    @Override
    public String getHelp() {
        return "</sendbottle:1089656103391985667> - Send a message in bottle into the seas for someone to read.";
    }

    @Override
    public String[] getTriggers() {
        return "sendbottle,mibsend".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
