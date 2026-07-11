package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.service.ProfileChecklist;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileChecklistTest {
    @Test
    void nextStepGuidesIncompleteProfile() {
        MatchUser user = new MatchUser("1");
        user.setTermsVersionAccepted("v1");
        user.setAgeCohort(AgeCohort.AGE_18_PLUS);
        MatchProfile profile = new MatchProfile("1");
        profile.setAgeCohort(AgeCohort.AGE_18_PLUS);
        assertTrue(ProfileChecklist.nextStep(user, profile).toLowerCase().contains("setup")
                || ProfileChecklist.nextStep(user, profile).toLowerCase().contains("finish"));
        profile.setDisplayName("Alex");
        profile.setBio("hi there friends");
        profile.setInterests(List.of("music"));
        profile.setState(ProfileState.DRAFT);
        assertTrue(ProfileChecklist.readyToSubmit(profile));
        String next = ProfileChecklist.nextStep(user, profile).toLowerCase();
        assertTrue(next.contains("setup") || next.contains("finish"));
        assertFalse(ProfileChecklist.format(user, profile).isBlank());
    }
}
