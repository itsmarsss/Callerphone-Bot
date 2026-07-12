package com.itsmarsss.callerphone.match;

import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.service.ProfileChecklist;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileChecklistTest {

    @Test
    void nextStepWalksThroughFields() {
        MatchUser user = new MatchUser("u1");
        assertTrue(ProfileChecklist.nextStep(user, null).toLowerCase().contains("age"));

        user.setAgeCohort(AgeCohort.AGE_18_PLUS);
        MatchProfile p = new MatchProfile();
        assertTrue(ProfileChecklist.nextStep(user, p).toLowerCase().contains("name"));

        p.setDisplayName("Alex");
        assertTrue(ProfileChecklist.nextStep(user, p).toLowerCase().contains("bio"));

        p.setBio("Hello there");
        assertTrue(ProfileChecklist.nextStep(user, p).toLowerCase().contains("interest"));

        p.setInterests(List.of("music"));
        p.setAgeCohort(AgeCohort.AGE_18_PLUS);
        assertTrue(ProfileChecklist.nextStep(user, p).toLowerCase().contains("live"));

        p.setState(ProfileState.ACTIVE);
        assertTrue(ProfileChecklist.nextStep(user, p).toLowerCase().contains("browse"));
    }

    @Test
    void readyToSubmitRequiresCoreFields() {
        MatchProfile p = new MatchProfile();
        assertFalse(ProfileChecklist.readyToSubmit(p));
        p.setAgeCohort(AgeCohort.AGE_18_PLUS);
        p.setDisplayName("A");
        p.setBio("B");
        p.setInterests(List.of("x"));
        assertTrue(ProfileChecklist.readyToSubmit(p));
    }
}
