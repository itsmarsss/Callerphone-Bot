package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.discord.match.MatchEmbeds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedInterestsTest {

    @Test
    void findsCaseInsensitiveOverlap() {
        MatchProfile a = new MatchProfile();
        a.setInterests(List.of("Gaming", "music", "art"));
        MatchProfile b = new MatchProfile();
        b.setInterests(List.of("gaming", "Hiking"));
        List<String> shared = MatchEmbeds.sharedInterests(a, b);
        assertEquals(1, shared.size());
        assertTrue(shared.get(0).equalsIgnoreCase("gaming"));
    }

    @Test
    void emptyWhenNoOverlap() {
        MatchProfile a = new MatchProfile();
        a.setInterests(List.of("a"));
        MatchProfile b = new MatchProfile();
        b.setInterests(List.of("b"));
        assertTrue(MatchEmbeds.sharedInterests(a, b).isEmpty());
    }
}
