package com.itsmarsss.callerphone;

/**
 * Centralized constants for the Callerphone bot.
 */
public final class Constants {

    // Cooldown durations (milliseconds)
    public static final long MESSAGE_COOLDOWN = 500;
    public static final long CREDIT_COOLDOWN = 15_000;
    public static final long COMMAND_COOLDOWN = 3_000;
    public static final long FINDBOTTLE_COOLDOWN = 600_000;
    public static final long SENDBOTTLE_COOLDOWN = 600_000;

    // Message in Bottle limits
    public static final int MIB_MIN_PAGE_LENGTH = 10;
    public static final int MIB_MAX_PAGE_LENGTH = 1500;
    /** Max pages per bottle thread (launch + replies). At cap, continue via Match. */
    public static final int MIB_MAX_PAGES = 8;

    // User limits
    public static final int USER_MAX_GAMES = 10;
    public static final int PREFIX_MAX_LENGTH = 15;
    public static final int PREFIX_MIN_LEVEL = 50;

    // Message limits
    public static final int MAX_MESSAGE_LENGTH = 1500;

    // TicTacToe game
    public static final int TICTACTOE_MAX_STAGE = 9;

    // Custom ID parsing
    public static final String CUSTOM_ID_DELIMITER = "-";

    private Constants() {
    }
}
