package com.itsmarsss.callerphone.msginbottle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BottleComponentIdsTest {

    @Test
    void roundTripsFindAndSend() {
        String find = BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_");
        BottleComponentIds.Parsed parsed = BottleComponentIds.parse(find);
        assertNotNull(parsed);
        assertEquals(BottleComponentIds.ACTION_FIND, parsed.action());
        assertEquals("_", parsed.opaqueId());
    }

    @Test
    void replyIdsIncludeBottleId() {
        String id = BottleComponentIds.of(BottleComponentIds.ACTION_REPLY_SIGN, "abc123");
        BottleComponentIds.Parsed parsed = BottleComponentIds.parse(id);
        assertNotNull(parsed);
        assertEquals(BottleComponentIds.ACTION_REPLY_SIGN, parsed.action());
        assertEquals("abc123", parsed.opaqueId());
        assertTrue(id.length() <= 100);
    }
}
