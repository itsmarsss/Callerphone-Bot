package com.itsmarsss.callerphone.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgeCohortTest {
    @Test
    void parsesCodes() {
        assertEquals(AgeCohort.AGE_13_15, AgeCohort.fromCode("13_15").orElseThrow());
        assertEquals(AgeCohort.AGE_16_17, AgeCohort.fromCode("16_17").orElseThrow());
        assertEquals(AgeCohort.AGE_18_PLUS, AgeCohort.fromCode("18_plus").orElseThrow());
        assertTrue(AgeCohort.fromCode("nope").isEmpty());
    }

    @Test
    void cohortsAreDistinct() {
        assertEquals(3, AgeCohort.values().length);
    }
}
