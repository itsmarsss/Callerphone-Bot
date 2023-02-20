package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;

public enum PoolResponse {

    END_POOL_SUCCESS("Successfully ended channel pool!"),
    HOST_POOL_SUCCESS("Successfully hosted channel pool for `#%s`!"),
    JOIN_POOL_SUCCESS("Successfully joined channel pool hosted by `ID: %s`(#%s)!"),
    KICK_POOL_SUCCESS("Successfully kicked `ID: %s` from this pool!"),
    LEAVE_POOL_SUCCESS("Successfully left channel pool!"),


    ALREADY_FULL("This pool is already at capacity."),
    ALREADY_HOSTING("This channel is already hosting a pool."),
    ALREADY_IN_POOL("This channel is already in a pool."),
    JOIN_FULL_POOL("Channel `ID: %s` attempted to join this full pool."),
    JOIN_INCORRECT_PWD("Channel `ID: %s` attempted to join with incorrect password."),
    KICKED_FROM_POOL("You have been kicked from the pool."),
    NOT_HOSTING("This channel is not hosting a pool."),
    NOT_IN_POOL("This channel is not in a pool."),
    POOL_END_WITH("End pool with: `%sendpool`"),
    POOL_HAS_CAPACITY("This pool now has capacity **%s**."),
    POOL_ID("This channel's pool ID is: `%s`"),
    POOL_LEAVE_WITH("Leave pool with: `%sleavepool`"),
    POOL_PUB("This pool is now **%s**."),
    POOL_PWD("This channel's password is: ||`%s`||"),
    POOL_SET_PWD("Set a password with: `%spoolpwd <password>`"),
    REQUESTED_ID_NOT_FOUND("Requested pool `ID: %s` does not exist."),
    REQUESTED_NOT_FOUND("Requested pool not found.");

    public final String label;

    PoolResponse(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
