package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.match.validation.ProfileValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileValidatorTest {
    @Test
    void rejectsContactAndUrls() {
        assertTrue(ProfileValidator.containsContactOrUrl("add me on discord.gg/xyz"));
        assertTrue(ProfileValidator.containsContactOrUrl("email me at a@b.com"));
        assertFalse(ProfileValidator.containsContactOrUrl("I like hiking and music"));
    }

    @Test
    void validatesDisplayName() {
        assertTrue(ProfileValidator.validateDisplayName("").size() > 0);
        assertTrue(ProfileValidator.validateDisplayName("Alex").isEmpty());
    }

    @Test
    void validatesInterestsCount() {
        assertTrue(ProfileValidator.validateInterests(List.of()).size() > 0);
        assertTrue(ProfileValidator.validateInterests(List.of("gaming", "music")).isEmpty());
    }
}
