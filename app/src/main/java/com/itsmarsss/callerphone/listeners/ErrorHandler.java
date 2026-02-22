package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Response;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized error handling for all interaction types
 */
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    /**
     * Handle error for slash command interactions
     */
    public static void handleSlashCommandError(SlashCommandInteractionEvent event, Exception error) {
        logger.error("Error in slash command: " + event.getName(), error);
        try {
            event.reply(String.format(Response.ERROR_MSG.toString(), error.toString())).setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    /**
     * Handle error for button interactions
     */
    public static void handleButtonError(ButtonInteractionEvent event, Exception error) {
        logger.error("Error in button interaction: " + event.getButton().getCustomId(), error);
        try {
            event.reply(String.format(Response.ERROR_MSG.toString(), error.toString())).setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    /**
     * Handle error for modal interactions
     */
    public static void handleModalError(ModalInteractionEvent event, Exception error) {
        logger.error("Error in modal interaction: " + event.getModalId(), error);
        try {
            event.reply(String.format(Response.ERROR_MSG.toString(), error.toString())).setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }

    /**
     * Handle error for message events
     */
    public static void handleMessageError(MessageReceivedEvent event, Exception error) {
        logger.error("Error in message event", error);
        try {
            event.getMessage().reply(String.format(Response.ERROR_MSG.toString(), error.toString())).queue();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
    }
}
