package com.itsmarsss.callerphone.experience;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceRendererTest {

    @Test
    void clampsTitleAndDescription() {
        ExperienceView view = ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("T".repeat(300))
                .description("D".repeat(5000))
                .build();
        MessageEmbed embed = ExperienceRenderer.toEmbed(view);
        assertTrue(embed.getTitle().length() <= DiscordLimits.EMBED_TITLE);
        assertTrue(embed.getDescription().length() <= DiscordLimits.EMBED_DESCRIPTION);
    }

    @Test
    void capsFieldCount() {
        List<ViewField> fields = new ArrayList<>();
        for (int i = 0; i < DiscordLimits.EMBED_FIELDS + 5; i++) {
            fields.add(ViewField.of("f" + i, "v" + i));
        }
        ExperienceView view = ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Fields")
                .fields(fields)
                .build();
        assertEquals(DiscordLimits.EMBED_FIELDS, ExperienceRenderer.toEmbed(view).getFields().size());
    }

    @Test
    void skipsInvalidComponentIds() {
        ExperienceView view = ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Actions")
                .actions(
                        ActionSpec.primary("ok-id", "OK"),
                        ActionSpec.secondary("x".repeat(DiscordLimits.CUSTOM_ID + 1), "Bad")
                )
                .build();
        assertEquals(1, ExperienceRenderer.toComponents(view).get(0).getComponents().size());
    }

    @Test
    void clampsButtonLabels() {
        ExperienceView view = ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Btn")
                .actions(ActionSpec.primary("id-1", "L".repeat(200)))
                .build();
        var rows = ExperienceRenderer.toComponents(view);
        assertEquals(1, rows.size());
        String label = rows.get(0).getButtons().get(0).getLabel();
        assertTrue(label.length() <= DiscordLimits.BUTTON_LABEL);
    }

    @Test
    void packsButtonsIntoRows() {
        List<ActionSpec> actions = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            actions.add(ActionSpec.secondary("id-" + i, "A" + i));
        }
        ExperienceView view = ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Many")
                .actions(actions)
                .build();
        assertEquals(3, ExperienceRenderer.toComponents(view).size());
    }

    @Test
    void toMessageIncludesEmbed() {
        MessageCreateData data = ExperienceRenderer.toMessage(
                ExperienceView.builder(ExperienceIntent.PROGRESS).title("Queue").description("Waiting").build()
        );
        assertFalse(data.getEmbeds().isEmpty());
    }

    @Test
    void expiredHelperUsesWarningIntent() {
        ExperienceView view = ExperienceRenderer.expired("Gone");
        assertEquals(ExperienceIntent.WARNING, view.intent());
        assertEquals("Gone", view.title());
        assertTrue(view.ephemeral());
    }
}
