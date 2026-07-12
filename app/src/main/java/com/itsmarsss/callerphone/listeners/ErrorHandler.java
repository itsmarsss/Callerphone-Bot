package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.call.discord.CallComponentIds;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.msginbottle.BottleComponentIds;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized error handling for all interaction types.
 * Plan philosophy §J — short recovery copy, optional reference, no stack dumps to users.
 */
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    public static void handleSlashCommandError(SlashCommandInteractionEvent event, Exception error) {
        logger.error("Error in slash command: {}", event.getName(), error);
        try {
            event.reply(recoveryMessage(error)).setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    public static void handleButtonError(ButtonInteractionEvent event, Exception error) {
        logger.error("Error in button interaction: {}", event.getButton().getCustomId(), error);
        try {
            event.reply(recoveryMessage(error)).setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    public static void handleModalError(ModalInteractionEvent event, Exception error) {
        logger.error("Error in modal interaction: {}", event.getModalId(), error);
        try {
            event.reply(recoveryMessage(error)).setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    public static void handleSelectError(StringSelectInteractionEvent event, Exception error) {
        logger.error("Error in string select: {}", event.getComponentId(), error);
        try {
            event.reply(recoveryMessage(error)).setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    public static void handleMessageError(MessageReceivedEvent event, Exception error) {
        logger.error("Error in message event", error);
        try {
            String support = Callerphone.config != null ? Callerphone.config.getSupportServer() : "";
            event.getMessage().reply(Response.ERROR_MSG.format(shortRef(error), support)).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    private static MessageCreateData recoveryMessage(Exception error) {
        String support = Callerphone.config != null ? Callerphone.config.getSupportServer() : "";
        String ref = shortRef(error);
        String body = "We couldn't finish that. Nothing was lost — try again.\n\n_Ref: `" + ref + "`_";
        if (support != null && !support.isBlank()) {
            body += "\nIf it keeps happening: " + support;
        }
        return ExperienceRenderer.toMessage(
                ExperienceView.builder(ExperienceIntent.WARNING)
                        .title("Something went wrong")
                        .description(body)
                        .actions(
                                ActionSpec.success(
                                        MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"),
                                        "Home"
                                ),
                                ActionSpec.primary(CallComponentIds.again("_"), "Start a call"),
                                ActionSpec.secondary(
                                        BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"),
                                        "Find a bottle"
                                ),
                                ActionSpec.secondary(
                                        MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                                        "Discover"
                                )
                        )
                        .ephemeral(true)
                        .build()
        );
    }

    private static String shortRef(Exception error) {
        if (error == null) {
            return "unknown";
        }
        String name = error.getClass().getSimpleName();
        int hash = Math.abs(System.identityHashCode(error) % 100000);
        return name + "-" + hash;
    }
}
