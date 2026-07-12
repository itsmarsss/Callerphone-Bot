package com.itsmarsss.callerphone.msginbottle.commands;

import com.itsmarsss.ICommand;
import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * Plan §25–27: consolidated bottle surface.
 * Legacy /sendbottle, /findbottle, /viewbottle remain registered as aliases.
 */
public final class BottleCommand implements ISlashCommand, ICommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String sub = e.getSubcommandName();
        if (sub == null) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.DISCOVERY)
                    .title("Message in a bottle")
                    .description("Send something into the sea, or find a bottle someone else cast.")
                    .build())).setEphemeral(true).queue();
            return;
        }
        switch (sub) {
            case "send" -> openSend(e);
            case "find" -> find(e);
            case "saved", "view" -> view(e);
            default -> e.reply(Response.MISSING_PARAM.toString()).setEphemeral(true).queue();
        }
    }

    private void openSend(SlashCommandInteractionEvent e) {
        if (e.getUser() == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }
        String userId = e.getUser().getId();
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(userId);
        if (elapsed < ToolSet.SENDBOTTLE_COOLDOWN) {
            long minutes = Math.max(1, (ToolSet.SENDBOTTLE_COOLDOWN - elapsed) / 60_000);
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                    .title("Not yet")
                    .description("Your next bottle can launch in **" + minutes + "** minute(s).")
                    .build())).setEphemeral(true).queue();
            return;
        }

        TextInput message = TextInput.create("message", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Write something another Callerphone user can discover later")
                .setMinLength(Constants.MIB_MIN_PAGE_LENGTH)
                .setMaxLength(Constants.MIB_MAX_PAGE_LENGTH)
                .build();
        TextInput signed = TextInput.create("signed", TextInputStyle.SHORT)
                .setPlaceholder("true = signed, false = anonymous")
                .setMinLength(4)
                .setMaxLength(5)
                .setValue("false")
                .build();
        e.replyModal(Modal.create("sendMIB", "Send a bottle")
                .addComponents(
                        Label.of("Message", message),
                        Label.of("Signed? (true / false)", signed)
                )
                .build()).queue();
    }

    private void find(SlashCommandInteractionEvent e) {
        String userId = e.getUser().getId();
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(userId);
        if (elapsed < ToolSet.FINDBOTTLE_COOLDOWN) {
            long minutes = Math.max(1, (ToolSet.FINDBOTTLE_COOLDOWN - elapsed) / 60_000);
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                    .title("Cooldown")
                    .description("Try finding another bottle in **" + minutes + "** minute(s).")
                    .build())).setEphemeral(true).queue();
            return;
        }
        Bottle bottle = MessageInBottle.findBottle();
        if (bottle == null) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.DISCOVERY)
                    .title("The water is quiet")
                    .description(
                            "No new bottles are available right now.\n\n"
                                    + "Cast one with `/bottle send`, or try again later."
                    )
                    .build())).setEphemeral(true).queue();
            return;
        }
        MessageCreateData message = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (message == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }
        Cooldown.setMIBFindCoolDown(userId);
        e.reply(message).setEphemeral(true).queue();
    }

    private void view(SlashCommandInteractionEvent e) {
        OptionMapping idOpt = e.getOption("id");
        if (idOpt == null || idOpt.getAsString().trim().isEmpty()) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                    .title("Saved bottles")
                    .description(
                            "Pass a bottle id to reopen one, or cast something new.\n\n"
                                    + "`/bottle find` · discover a bottle\n"
                                    + "`/bottle send` · cast one"
                    )
                    .build())).setEphemeral(true).queue();
            return;
        }
        Bottle bottle = MIB.getBottle(idOpt.getAsString().trim());
        if (bottle == null) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                    .title("Not found")
                    .description("No bottle with that id.")
                    .build())).setEphemeral(true).queue();
            return;
        }
        MessageCreateData message = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (message == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }
        e.reply(message).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "`/bottle send` cast a bottle\n"
                + "`/bottle find` discover one\n"
                + "`/bottle saved` reopen by id";
    }

    @Override
    public String getName() {
        return "bottle";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Message in a bottle")
                .addSubcommands(
                        new SubcommandData("send", "Cast a bottle into the sea"),
                        new SubcommandData("find", "Find a random bottle"),
                        new SubcommandData("saved", "Open a bottle by id")
                                .addOptions(new OptionData(OptionType.STRING, "id", "Bottle id", false))
                )
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
