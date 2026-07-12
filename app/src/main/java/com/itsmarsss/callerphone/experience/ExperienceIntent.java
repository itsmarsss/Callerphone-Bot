package com.itsmarsss.callerphone.experience;

/**
 * Semantic visual treatment shared across Match, Call, bottles, and account screens.
 * Presenters choose intent; the renderer maps it to theme colors and layout emphasis.
 */
public enum ExperienceIntent {
    SOCIAL,
    SUCCESS,
    PROGRESS,
    DISCOVERY,
    PREMIUM,
    WARNING,
    ERROR,
    SAFETY,
    NEUTRAL
}
