package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
