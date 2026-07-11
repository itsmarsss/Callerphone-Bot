package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.model.Match;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchPairKeyTest {
    @Test
    void pairKeyIsOrderIndependent() {
        assertEquals(Match.pairKeyFor("a", "b"), Match.pairKeyFor("b", "a"));
        assertEquals("111:222", Match.pairKeyFor("222", "111"));
    }
}
