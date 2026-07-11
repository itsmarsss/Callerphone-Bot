package com.itsmarsss.callerphone.utils;

import com.itsmarsss.callerphone.Constants;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

public class InteractionUtils {

    public static final String CUSTOM_ID_DELIMITER = Constants.CUSTOM_ID_DELIMITER;

    /**
     * Parse custom ID by splitting on delimiter
     */
    public static String[] parseCustomId(String customId) {
        return customId.split(CUSTOM_ID_DELIMITER);
    }

    /**
     * Parse boolean from modal value with validation
     *
     * @return Boolean value, or null if invalid
     */
    public static Boolean parseBooleanFromModal(ModalInteractionEvent event, String fieldId) {
        ModalMapping mapping = event.getValue(fieldId);
        if (mapping == null) {
            return null;
        }

        String value = mapping.getAsString().toLowerCase();

        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        }

        return null;
    }

    /**
     * Parse integer from modal value with validation
     *
     * @return Integer value, or null if invalid
     */
    public static Integer parseIntegerFromModal(ModalInteractionEvent event, String fieldId) {
        ModalMapping mapping = event.getValue(fieldId);
        if (mapping == null) {
            return null;
        }

        try {
            return Integer.parseInt(mapping.getAsString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
