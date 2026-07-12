package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.DiscordLimits;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;

/** Plan §27 — saved / threads list surfaces. */
public final class BottleListUi {
    private BottleListUi() {
    }

    public static MessageCreateData threads(List<Bottle> bottles) {
        if (bottles == null || bottles.isEmpty()) {
            return ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.DISCOVERY)
                    .title("Your bottle threads")
                    .description("No threads yet. Find a bottle and reply to start one.")
                    .actions(
                            ActionSpec.success(
                                    BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"),
                                    "Find a bottle"
                            ),
                            ActionSpec.primary(
                                    BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"),
                                    "Send a bottle"
                            )
                    )
                    .ephemeral(true)
                    .build());
        }
        StringBuilder body = new StringBuilder();
        StringSelectMenu.Builder menu = StringSelectMenu.create(
                        BottleComponentIds.of(BottleComponentIds.ACTION_THREAD_MENU, "_"))
                .setPlaceholder("Open thread")
                .setRequiredRange(1, 1);
        int i = 0;
        for (Bottle bottle : bottles) {
            if (i >= DiscordLimits.SELECT_OPTIONS) {
                break;
            }
            int replies = Math.max(0, bottle.getPages().size() - 1);
            String preview = MIB.preview(bottle, 60);
            body.append("**").append(i + 1).append(".** ")
                    .append(preview)
                    .append(replies > 0 ? " · " + replies + " reply" + (replies == 1 ? "" : "ies") : " · launched")
                    .append("\n");
            String label = truncate(preview, 80);
            menu.addOption(
                    label.isBlank() ? "Bottle " + (i + 1) : label,
                    bottle.getId(),
                    replies + " reply" + (replies == 1 ? "" : "ies")
            );
            i++;
        }
        return new MessageCreateBuilder()
                .setEmbeds(ExperienceRenderer.toEmbed(ExperienceView.builder(ExperienceIntent.DISCOVERY)
                        .title("Your bottle threads")
                        .description(body.toString().trim())
                        .ephemeral(true)
                        .build()))
                .setComponents(ActionRow.of(menu.build()))
                .build();
    }

    public static MessageCreateData saved(List<Bottle> bottles) {
        if (bottles == null || bottles.isEmpty()) {
            return ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                    .title("Saved bottles")
                    .description(
                            "Nothing saved yet. Tap **Save** on a bottle you find, "
                                    + "or open one by id with `/bottle saved id:`."
                    )
                    .actions(
                            ActionSpec.success(
                                    BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"),
                                    "Find a bottle"
                            ),
                            ActionSpec.primary(
                                    BottleComponentIds.of(BottleComponentIds.ACTION_SEND, "_"),
                                    "Send a bottle"
                            )
                    )
                    .ephemeral(true)
                    .build());
        }
        StringBuilder body = new StringBuilder();
        StringSelectMenu.Builder menu = StringSelectMenu.create(
                        BottleComponentIds.of(BottleComponentIds.ACTION_SAVED_MENU, "_"))
                .setPlaceholder("Choose a bottle")
                .setRequiredRange(1, 1);
        int i = 0;
        for (Bottle bottle : bottles) {
            if (i >= DiscordLimits.SELECT_OPTIONS) {
                break;
            }
            int pages = bottle.getPages() == null ? 0 : bottle.getPages().size();
            String preview = MIB.preview(bottle, 60);
            body.append("**").append(i + 1).append(".** ")
                    .append(preview)
                    .append(" · ").append(pages).append(" page").append(pages == 1 ? "" : "s")
                    .append("\n");
            menu.addOption(
                    truncate(preview, 80).isBlank() ? "Bottle " + (i + 1) : truncate(preview, 80),
                    bottle.getId(),
                    pages + " page" + (pages == 1 ? "" : "s")
            );
            i++;
        }
        return new MessageCreateBuilder()
                .setEmbeds(ExperienceRenderer.toEmbed(ExperienceView.builder(ExperienceIntent.DISCOVERY)
                        .title("Saved bottles")
                        .description(body.toString().trim())
                        .ephemeral(true)
                        .build()))
                .setComponents(ActionRow.of(menu.build()))
                .build();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
