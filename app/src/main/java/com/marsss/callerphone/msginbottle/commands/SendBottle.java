package com.marsss.callerphone.msginbottle.commands;

import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.ISlashCommand;
import com.marsss.database.categories.Cooldown;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

public class SendBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(e.getMember().getId()) < ToolSet.SENDBOTTLE_COOLDOWN) {
            e.reply(":warning: **Send MIB Cooldown;** " + ((ToolSet.SENDBOTTLE_COOLDOWN - (System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(e.getMember().getId()))) / 60000) + " minute(s)").setEphemeral(true).queue();
            return;
        }
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


        Modal modal = Modal.create("sendMIB", "Send Message In Bottle")
                .addComponents(Label.of("Message (10 - 1500 characters)", message), Label.of("Page Signed (true | false)", signed))
                .build();

        e.replyModal(modal).queue();
    }


    @Override
    public String getHelp() {
        return "</sendbottle:1089656103391985667> - Send a message in bottle into the seas for someone to read.";
    }

    @Override
    public String getName() {
        return "sendbottle";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .setContexts(InteractionContextType.GUILD);
    }
}
