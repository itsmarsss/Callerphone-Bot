package com.itsmarsss.callerphone.call.model;

/**
 * One side of a call. Today: a guild text channel + the user who started the call.
 * Future hourly matches may use a private channel / user DM id instead.
 */
public record CallEndpoint(
        String channelId,
        String starterUserId,
        EndpointKind kind
) {
    public enum EndpointKind {
        GUILD_TEXT,
        /** Reserved for scheduled DM-style links */
        USER_DM
    }

    public static CallEndpoint guild(String channelId, String starterUserId) {
        return new CallEndpoint(channelId, starterUserId, EndpointKind.GUILD_TEXT);
    }
}
