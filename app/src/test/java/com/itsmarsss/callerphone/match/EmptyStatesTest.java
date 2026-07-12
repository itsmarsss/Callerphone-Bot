package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.service.EmptyStates;
import com.itsmarsss.callerphone.match.service.UpsellCopy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmptyStatesTest {

    @Test
    void limitCopyIsUserFacing() {
        assertFalse(EmptyStates.interestLimit().isBlank());
        assertFalse(EmptyStates.conversationLimit().isBlank());
        assertTrue(EmptyStates.noCandidates().toLowerCase().contains("seen"));
    }

    @Test
    void upsellDelegatesInterestAndChats() {
        assertEquals(EmptyStates.interestLimit(), UpsellCopy.forLimit("interest"));
        assertEquals(EmptyStates.conversationLimit(), UpsellCopy.forLimit("conversations"));
        assertTrue(UpsellCopy.forLimit("discovery").toLowerCase().contains("discoveries"));
    }
}
