package com.itsmarsss.callerphone.experience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscordLimitsTest {

    @Test
    void clampLeavesShortValues() {
        assertEquals("hello", DiscordLimits.clamp("hello", 10));
    }

    @Test
    void clampTruncatesWithEllipsis() {
        String out = DiscordLimits.clamp("abcdefghij", 5);
        assertEquals(5, out.length());
        assertTrue(out.endsWith("…"));
    }

    @Test
    void clampNullSafe() {
        assertNull(DiscordLimits.clamp(null, 10));
    }

    @Test
    void customIdLength() {
        assertTrue(DiscordLimits.isValidCustomId("c-v1-share-abc"));
        assertFalse(DiscordLimits.isValidCustomId(""));
        assertFalse(DiscordLimits.isValidCustomId(null));
        assertFalse(DiscordLimits.isValidCustomId("x".repeat(DiscordLimits.CUSTOM_ID + 1)));
        assertTrue(DiscordLimits.isValidCustomId("x".repeat(DiscordLimits.CUSTOM_ID)));
    }

    @Test
    void modalLabelLimit() {
        assertTrue(DiscordLimits.isValidModalLabel("Display name"));
        assertFalse(DiscordLimits.isValidModalLabel("x".repeat(DiscordLimits.MODAL_LABEL + 1)));
        assertTrue(DiscordLimits.isValidModalLabel("x".repeat(DiscordLimits.MODAL_LABEL)));
    }

    @Test
    void buttonLabelLimit() {
        assertTrue(DiscordLimits.isValidButtonLabel("Discover"));
        assertFalse(DiscordLimits.isValidButtonLabel("x".repeat(DiscordLimits.BUTTON_LABEL + 1)));
    }

    @Test
    void platformConstantsMatchDiscordDocs() {
        assertEquals(256, DiscordLimits.EMBED_TITLE);
        assertEquals(4096, DiscordLimits.EMBED_DESCRIPTION);
        assertEquals(1024, DiscordLimits.EMBED_FIELD_VALUE);
        assertEquals(25, DiscordLimits.EMBED_FIELDS);
        assertEquals(80, DiscordLimits.BUTTON_LABEL);
        assertEquals(100, DiscordLimits.CUSTOM_ID);
        assertEquals(5, DiscordLimits.ACTION_ROWS);
        assertEquals(5, DiscordLimits.BUTTONS_PER_ROW);
        assertEquals(25, DiscordLimits.SELECT_OPTIONS);
        assertEquals(45, DiscordLimits.MODAL_TITLE);
        assertEquals(45, DiscordLimits.MODAL_LABEL);
        assertEquals(5, DiscordLimits.MODAL_FIELDS);
    }
}
