package com.itsmarsss.callerphone.msginbottle.commands;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Cooldown;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.modals.Modal;

public class SendBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (e.getMember() == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }

        String userId = e.getMember().getId();
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(userId);
        if (elapsed < ToolSet.SENDBOTTLE_COOLDOWN) {
            long minutes = Math.max(1, (ToolSet.SENDBOTTLE_COOLDOWN - elapsed) / 60_000);
            e.reply(":warning: **Send MIB Cooldown;** " + minutes + " minute(s)")
                    .setEphemeral(true).queue();
            return;
        }

        TextInput message = TextInput.create("message", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Write your message in a bottle here")
                .setMinLength(Constants.MIB_MIN_PAGE_LENGTH)
                .setMaxLength(Constants.MIB_MAX_PAGE_LENGTH)
                .build();

        TextInput signed = TextInput.create("signed", TextInputStyle.SHORT)
                .setPlaceholder("true or false")
                .setMinLength(4)
                .setMaxLength(5)
                .setValue("true")
                .build();

        Modal modal = Modal.create("sendMIB", "Send Message In Bottle")
                .addComponents(
                        Label.of("Message (" + Constants.MIB_MIN_PAGE_LENGTH + " - " + Constants.MIB_MAX_PAGE_LENGTH + " characters)", message),
                        Label.of("Page Signed (true | false)", signed)
                )
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
