package com.itsmarsss.callerphone.call;

import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.service.CallQueueService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CallQueueServiceTest {
    @Test
    void matchesGuildChannelsTogether() {
        CallQueueService q = new CallQueueService();
        q.enqueue(CallEndpoint.guild("a", "u1"));
        assertEquals(1, q.size(CallEndpoint.EndpointKind.GUILD_TEXT));
        assertTrue(q.dequeuePeer(CallEndpoint.guild("b", "u2")).isPresent());
        assertEquals(0, q.size(CallEndpoint.EndpointKind.GUILD_TEXT));
    }

    @Test
    void doesNotCrossMatchGuildAndDm() {
        CallQueueService q = new CallQueueService();
        q.enqueue(CallEndpoint.guild("guild-ch", "u1"));
        assertTrue(q.dequeuePeer(CallEndpoint.dm("dm-ch", "u2")).isEmpty());
        assertEquals(1, q.size(CallEndpoint.EndpointKind.GUILD_TEXT));
        assertEquals(0, q.size(CallEndpoint.EndpointKind.USER_DM));
    }

    @Test
    void matchesDmsTogether() {
        CallQueueService q = new CallQueueService();
        q.enqueue(CallEndpoint.dm("dm-a", "u1"));
        assertTrue(q.dequeuePeer(CallEndpoint.dm("dm-b", "u2")).isPresent());
        assertEquals(0, q.size(CallEndpoint.EndpointKind.USER_DM));
    }

    @Test
    void doesNotMatchSameUser() {
        CallQueueService q = new CallQueueService();
        q.enqueue(CallEndpoint.dm("dm-a", "u1"));
        assertTrue(q.dequeuePeer(CallEndpoint.dm("dm-b", "u1")).isEmpty());
    }
}
