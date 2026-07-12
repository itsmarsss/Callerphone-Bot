package com.itsmarsss.callerphone.experience;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns {@link ExperienceView} into valid Discord payloads.
 * Enforces embed, button, and action-row limits.
 */
public final class ExperienceRenderer {
    private ExperienceRenderer() {
    }

    public static MessageEmbed toEmbed(ExperienceView view) {
        EmbedBuilder emb = new EmbedBuilder()
                .setColor(ExperienceTheme.color(view.intent()));
        if (view.title() != null && !view.title().isBlank()) {
            emb.setTitle(DiscordLimits.clamp(view.title(), DiscordLimits.EMBED_TITLE));
        }
        if (view.description() != null && !view.description().isBlank()) {
            emb.setDescription(DiscordLimits.clamp(view.description(), DiscordLimits.EMBED_DESCRIPTION));
        }
        int fieldCount = 0;
        for (ViewField field : view.fields()) {
            if (fieldCount >= DiscordLimits.EMBED_FIELDS) {
                break;
            }
            if (field == null || field.name() == null || field.value() == null) {
                continue;
            }
            emb.addField(
                    DiscordLimits.clamp(field.name(), DiscordLimits.EMBED_FIELD_NAME),
                    DiscordLimits.clamp(field.value(), DiscordLimits.EMBED_FIELD_VALUE),
                    field.inline()
            );
            fieldCount++;
        }
        if (view.footer() != null && !view.footer().isBlank()) {
            emb.setFooter(DiscordLimits.clamp(view.footer(), DiscordLimits.EMBED_FOOTER));
        }
        if (view.media() != null) {
            if (view.media().thumbnailUrl() != null && !view.media().thumbnailUrl().isBlank()) {
                emb.setThumbnail(view.media().thumbnailUrl());
            }
            if (view.media().imageUrl() != null && !view.media().imageUrl().isBlank()) {
                emb.setImage(view.media().imageUrl());
            }
        }
        return emb.build();
    }

    public static List<ActionRow> toComponents(ExperienceView view) {
        List<Button> buttons = new ArrayList<>();
        for (ActionSpec action : view.actions()) {
            if (action == null || action.componentId() == null || action.componentId().isBlank()) {
                continue;
            }
            // Link buttons use URLs; Premium uses SKU ids; others use custom ids ≤100.
            if (action.style() != ActionSpec.Style.LINK
                    && action.style() != ActionSpec.Style.PREMIUM
                    && !DiscordLimits.isValidCustomId(action.componentId())) {
                continue;
            }
            if (action.style() == ActionSpec.Style.LINK && action.componentId().length() > 512) {
                continue;
            }
            Button button;
            if (action.style() == ActionSpec.Style.PREMIUM) {
                try {
                    button = Button.premium(net.dv8tion.jda.api.entities.SkuSnowflake.fromId(action.componentId()));
                } catch (Exception ex) {
                    continue;
                }
            } else {
                String label = DiscordLimits.clamp(action.label(), DiscordLimits.BUTTON_LABEL);
                if (label == null || label.isBlank()) {
                    continue;
                }
                button = switch (action.style()) {
                    case PRIMARY -> Button.primary(action.componentId(), label);
                    case SECONDARY -> Button.secondary(action.componentId(), label);
                    case SUCCESS -> Button.success(action.componentId(), label);
                    case DANGER -> Button.danger(action.componentId(), label);
                    case LINK -> Button.link(action.componentId(), label);
                    case PREMIUM -> throw new IllegalStateException("handled above");
                };
            }
            if (action.disabled() && action.style() != ActionSpec.Style.LINK
                    && action.style() != ActionSpec.Style.PREMIUM) {
                button = button.asDisabled();
            }
            buttons.add(button);
            if (buttons.size() >= DiscordLimits.ACTION_ROWS * DiscordLimits.BUTTONS_PER_ROW) {
                break;
            }
        }
        List<ActionRow> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size() && rows.size() < DiscordLimits.ACTION_ROWS; i += DiscordLimits.BUTTONS_PER_ROW) {
            int end = Math.min(i + DiscordLimits.BUTTONS_PER_ROW, buttons.size());
            rows.add(ActionRow.of(buttons.subList(i, end)));
        }
        return rows;
    }

    public static MessageCreateData toMessage(ExperienceView view) {
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setEmbeds(toEmbed(view));
        List<ActionRow> rows = toComponents(view);
        if (!rows.isEmpty()) {
            builder.setComponents(rows);
        }
        return builder.build();
    }

    public static MessageEditData toEdit(ExperienceView view) {
        MessageEditBuilder builder = new MessageEditBuilder()
                .setContent(null)
                .setEmbeds(toEmbed(view));
        List<ActionRow> rows = toComponents(view);
        if (rows.isEmpty()) {
            builder.setComponents();
        } else {
            builder.setComponents(rows);
        }
        return builder.build();
    }

    public static ExperienceView expired(String title) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title(title == null || title.isBlank() ? "That expired" : title)
                .description(CopyCatalog.expiredAction())
                .ephemeral(true)
                .build();
    }
}
