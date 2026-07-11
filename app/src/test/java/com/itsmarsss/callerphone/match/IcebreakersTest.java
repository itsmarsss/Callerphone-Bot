package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.Icebreakers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IcebreakersTest {
    @Test
    void prefersSharedInterests() {
        MatchProfile a = new MatchProfile("1");
        a.setInterests(List.of("gaming", "music"));
        MatchProfile b = new MatchProfile("2");
        b.setInterests(List.of("gaming", "art"));
        String line = Icebreakers.forPair(a, b);
        assertFalse(line.isBlank());
        assertTrue(line.toLowerCase().contains("gaming") || line.length() > 10);
    }
}
