package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchComponentIdsTest {

    @Test
    void roundTripsOpaqueIdsWithHyphens() {
        String id = MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, "abc-def-ghi");
        assertTrue(id.startsWith("m-v1-"));
        MatchComponentIds.Parsed parsed = MatchComponentIds.parse(id);
        assertNotNull(parsed);
        assertEquals(MatchComponentIds.ACTION_INTERESTED, parsed.action());
        assertEquals("abc-def-ghi", parsed.opaqueId());
    }

    @Test
    void parsesSafetyContext() {
        MatchComponentIds.SafetyContext profile = MatchComponentIds.parseSafetyContext("profile:user99");
        assertNotNull(profile);
        assertEquals(MatchComponentIds.SafetyKind.PROFILE, profile.kind());
        assertEquals("user99", profile.referenceId());
        assertEquals("profile:user99", profile.opaque());

        MatchComponentIds.SafetyContext chat = MatchComponentIds.parseSafetyContext("conversation:c1");
        assertNotNull(chat);
        assertEquals(MatchComponentIds.SafetyKind.CONVERSATION, chat.kind());
        assertEquals("c1", chat.referenceId());
    }

    @Test
    void rejectsInvalidSafetyContext() {
        assertNull(MatchComponentIds.parseSafetyContext(null));
        assertNull(MatchComponentIds.parseSafetyContext("_"));
        assertNull(MatchComponentIds.parseSafetyContext("nope"));
        assertNull(MatchComponentIds.parseSafetyContext("profile:"));
    }

    @Test
    void safetyOpenIdFitsDiscordLimit() {
        String id = MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_OPEN, "conversation:abc123");
        assertTrue(id.length() <= 100);
    }
}
