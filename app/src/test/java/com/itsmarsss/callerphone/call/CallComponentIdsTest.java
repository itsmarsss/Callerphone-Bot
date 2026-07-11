package com.itsmarsss.callerphone.call;

import com.itsmarsss.callerphone.call.discord.CallComponentIds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CallComponentIdsTest {
    @Test
    void parsesShareAndLike() {
        String share = CallComponentIds.share("sess1");
        CallComponentIds.Parsed p = CallComponentIds.parse(share);
        assertNotNull(p);
        assertEquals(CallComponentIds.SHARE, p.action());
        assertEquals("sess1", p.sessionId());

        String like = CallComponentIds.like("sess1", "123456");
        CallComponentIds.Parsed l = CallComponentIds.parse(like);
        assertNotNull(l);
        assertEquals(CallComponentIds.LIKE, l.action());
        assertEquals("sess1", l.sessionId());
        assertEquals("123456", l.subjectUserId());
    }
}
