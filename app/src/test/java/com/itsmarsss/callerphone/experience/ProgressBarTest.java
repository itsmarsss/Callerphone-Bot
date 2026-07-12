package com.itsmarsss.callerphone.experience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgressBarTest {

    @Test
    void emptyWhenMaxZero() {
        assertEquals("░░░░░░░░░░", ProgressBar.of(5, 0, 10));
    }

    @Test
    void fullAtMax() {
        assertEquals("██████████", ProgressBar.of(100, 100, 10));
    }

    @Test
    void halfProgress() {
        assertEquals("█████░░░░░", ProgressBar.of(50, 100, 10));
    }

    @Test
    void clampsOverflow() {
        assertEquals("██████████", ProgressBar.of(200, 100, 10));
    }
}
