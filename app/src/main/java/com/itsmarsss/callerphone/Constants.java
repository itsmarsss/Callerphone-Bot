package com.itsmarsss.callerphone;

/**
 * Centralized constants for the Callerphone bot
 */
public class Constants {

    // Cooldown durations (milliseconds)
    public static final long MESSAGE_COOLDOWN = 500;
    public static final long CREDIT_COOLDOWN = 15000;
    public static final long COMMAND_COOLDOWN = 3000;

    // Channel Pool limits
    public static final int POOL_MIN_CAPACITY = 2;
    public static final int POOL_MAX_CAPACITY = 10;
    public static final int POOL_DEFAULT_CAPACITY = 10;

    // Message in Bottle limits
    public static final int MIB_MIN_PAGE_LENGTH = 10;
    public static final int MIB_MAX_PAGE_LENGTH = 1500;

    // User limits
    public static final int USER_MAX_GAMES = 10;

    // TicTacToe game
    public static final int TICTACTOE_MAX_STAGE = 9;

    // Custom ID parsing
    public static final String CUSTOM_ID_DELIMITER = "-";

    private Constants() {
        // Prevent instantiation
    }
}
