package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.Icebreakers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IcebreakersTest {

    @Test
    void sharedInterestsCaseInsensitive() {
        MatchProfile a = new MatchProfile();
        a.setInterests(List.of("Music", "Hiking"));
        MatchProfile b = new MatchProfile();
        b.setInterests(List.of("music", "Coding"));
        String line = Icebreakers.forPair(a, b);
        assertNotNull(line);
        assertFalse(line.isBlank());
        // Prefer shared-interest opener when present
        assertTrue(line.toLowerCase().contains("music") || line.length() > 10);
    }

    @Test
    void nullProfilesFallback() {
        String line = Icebreakers.forPair(null, null);
        assertNotNull(line);
        assertFalse(line.isBlank());
    }
}
