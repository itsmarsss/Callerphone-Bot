package com.itsmarsss.callerphone.call;

import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.service.CallQueueService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallQueueServiceTest {
    @Test
    void matchesAnyWaitingChannelWithoutModes() {
        CallQueueService q = new CallQueueService();
        q.enqueue(CallEndpoint.guild("a", "u1"));
        assertEquals(1, q.size());
        assertTrue(q.dequeueOther("b").isPresent());
        assertEquals(0, q.size());
    }
}
