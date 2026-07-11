package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.service.EmptyStates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class EmptyStatesTest {
    @Test
    void messagesAreHelpful() {
        assertFalse(EmptyStates.noCandidates().isBlank());
        assertFalse(EmptyStates.noChats().isBlank());
        assertFalse(EmptyStates.notActive().isBlank());
    }
}
