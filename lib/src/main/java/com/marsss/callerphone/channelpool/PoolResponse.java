package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;

public enum PoolResponse {

    END_POOL_SUCCESS(ToolSet.CP_EMJ + "Successfully ended channel pool!"),
    HOST_POOL_SUCCESS(ToolSet.CP_EMJ + "Successfully hosted channel pool for `#%s`!"),
    JOIN_POOL_SUCCESS(ToolSet.CP_EMJ + "Successfully joined channel pool hosted by `ID: %s`(#%s)!"),
    KICK_POOL_SUCCESS(ToolSet.CP_EMJ + "Successfully kicked `ID: %s` from this pool!"),
    LEAVE_POOL_SUCCESS(ToolSet.CP_EMJ + "Successfully left channel pool!"),


    ALREADY_FULL(ToolSet.CP_EMJ + "This pool is already at capacity."),
    ALREADY_HOSTING(ToolSet.CP_EMJ + "This channel is already hosting a pool."),
    ALREADY_IN_POOL(ToolSet.CP_EMJ + "This channel is already in a pool."),
    JOIN_FULL_POOL(ToolSet.CP_EMJ + "Channel `ID: %s` attempted to join this full pool."),
    JOIN_INCORRECT_PWD(ToolSet.CP_EMJ + "Channel `ID: %s` attempted to join with incorrect password."),
    KICKED_FROM_POOL(ToolSet.CP_EMJ + "You have been kicked from the pool."),
    NOT_HOSTING(ToolSet.CP_EMJ + "This channel is not hosting a pool."),
    NOT_IN_POOL(ToolSet.CP_EMJ + "This channel is not in a pool."),
    POOL_END_WITH("End pool with: `" + Callerphone.config.getPrefix() + "endpool`"),
    POOL_HAS_CAPACITY(ToolSet.CP_EMJ + "This pool now has capacity **%s**."),
    POOL_ID("This channel's pool ID is: `%s`"),
    POOL_LEAVE_WITH("Leave pool with: `" + Callerphone.config.getPrefix() + "leavepool`"),
    LEFT_POOL(ToolSet.CP_EMJ + "Channel `ID: %s` has left this pool."),
    POOL_PUB(ToolSet.CP_EMJ + "This pool is now **%s**."),
    POOL_PWD("This channel's password is: %s"),
    POOL_SET_PWD("Set a password with: `" + Callerphone.config.getPrefix() + "poolpwd <password>`"),
    REQUESTED_ID_NOT_FOUND(ToolSet.CP_EMJ + "Requested pool `ID: %s` does not exist."),
    REQUESTED_NOT_FOUND(ToolSet.CP_EMJ + "Requested pool not found.");

    public final String label;

    PoolResponse(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}