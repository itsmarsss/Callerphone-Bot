package com.itsmarsss.callerphone.call.model;

/**
 * One side of a call: a guild text channel or a user DM with the bot.
 */
public record CallEndpoint(
        String channelId,
        String starterUserId,
        EndpointKind kind
) {
    public enum EndpointKind {
        GUILD_TEXT,
        USER_DM
    }

    public static CallEndpoint guild(String channelId, String starterUserId) {
        return new CallEndpoint(channelId, starterUserId, EndpointKind.GUILD_TEXT);
    }

    public static CallEndpoint dm(String privateChannelId, String userId) {
        return new CallEndpoint(privateChannelId, userId, EndpointKind.USER_DM);
    }

    public boolean isDm() {
        return kind == EndpointKind.USER_DM;
    }
}
