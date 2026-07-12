package com.itsmarsss.callerphone.experience;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceRendererTest {

    @Test
    void toMessageIncludesEmbedAndButtons() {
        ExperienceView view = ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Title")
                .description("Body text")
                .actions(
                        ActionSpec.success("m-v1-start_browse-_", "Discover"),
                        ActionSpec.secondary("c-v1-again-_", "Start a call")
                )
                .build();
        MessageCreateData data = ExperienceRenderer.toMessage(view);
        assertFalse(data.getEmbeds().isEmpty());
        assertEquals("Title", data.getEmbeds().get(0).getTitle());
        assertFalse(data.getComponents().isEmpty());
    }

    @Test
    void toEditClearsContent() {
        ExperienceView view = ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Hmm")
                .description("Something failed")
                .build();
        MessageEditData edit = ExperienceRenderer.toEdit(view);
        assertNotNull(edit);
        assertFalse(edit.getEmbeds().isEmpty());
    }

    @Test
    void dropsInvalidCustomIdsButKeepsLinks() {
        ExperienceView view = ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Links")
                .description("ok")
                .actions(
                        ActionSpec.primary("x".repeat(200), "Too long"),
                        ActionSpec.link("https://example.com/" + "a".repeat(50), "Invite")
                )
                .build();
        MessageCreateData data = ExperienceRenderer.toMessage(view);
        // Long custom id dropped; link kept
        assertFalse(data.getComponents().isEmpty());
    }

    @Test
    void expiredHelperHasWarningIntent() {
        ExperienceView view = ExperienceRenderer.expired("Gone");
        assertEquals(ExperienceIntent.WARNING, view.intent());
        assertEquals("Gone", view.title());
    }

    @Test
    void premiumSkuActionRendersComponent() {
        ExperienceView view = ExperienceView.builder(ExperienceIntent.PREMIUM)
                .title("Premium")
                .description("Compare plans")
                .actions(
                        ActionSpec.premiumSku("123456789012345678"),
                        ActionSpec.success("m-v1-start_browse-_", "Discover")
                )
                .build();
        MessageCreateData data = ExperienceRenderer.toMessage(view);
        assertFalse(data.getEmbeds().isEmpty());
        assertFalse(data.getComponents().isEmpty());
    }
}
