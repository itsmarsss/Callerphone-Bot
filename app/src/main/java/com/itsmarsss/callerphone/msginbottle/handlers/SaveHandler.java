package com.itsmarsss.callerphone.msginbottle.handlers;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

/**
 * Saves a bottle copy to DMs and bookmarks it for `/bottle saved`.
 * Custom id is plain {@code sve} (legacy) or may include bottle id in message content.
 */
public class SaveHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        if (!e.getMessage().getEmbeds().isEmpty()) {
            ToolSet.sendPrivateEmbed(e.getUser(), e.getMessage().getEmbeds().get(0));
        }

        // Best-effort extract bottle id from sibling button custom ids (adp-{id}, rpt-{id}-…)
        String bottleId = extractBottleId(e);
        if (bottleId != null && ApplicationContext.isReady()) {
            ApplicationContext.get().bottleSaves().save(e.getUser().getId(), bottleId);
        }

        e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Saved")
                .description(
                        "A copy is in your DMs"
                                + (bottleId != null
                                ? ". Reopen anytime with `/bottle saved`."
                                : ".")
                )
                .build())).setEphemeral(true).queue();
    }

    private static String extractBottleId(ButtonInteraction e) {
        try {
            for (var layout : e.getMessage().getComponents()) {
                if (!(layout instanceof net.dv8tion.jda.api.components.actionrow.ActionRow row)) {
                    continue;
                }
                for (var child : row.getComponents()) {
                    if (!(child instanceof net.dv8tion.jda.api.components.buttons.Button button)) {
                        continue;
                    }
                    String id = button.getCustomId();
                    if (id == null) {
                        continue;
                    }
                    if (id.startsWith("adp-") && id.length() > 4) {
                        return id.substring(4);
                    }
                    if (id.startsWith("rpt-")) {
                        String rest = id.substring(4);
                        int dash = rest.lastIndexOf('-');
                        if (dash > 0) {
                            return rest.substring(0, dash);
                        }
                    }
                    if (id.startsWith("nxp-") || id.startsWith("pvp-")) {
                        // nxp-{id}-{page}
                        String rest = id.substring(4);
                        int dash = rest.lastIndexOf('-');
                        if (dash > 0) {
                            return rest.substring(0, dash);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public String getID() {
        return "sve";
    }
}
