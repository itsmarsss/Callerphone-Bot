package com.itsmarsss.callerphone.match.model;

/**
 * Photo reference. Prefer Discord message/attachment storage for cheap hosting.
 * CDN URLs are resolved on demand from messageId when possible.
 */
public record MediaRef(
        String mediaId,
        String status,
        int sortOrder,
        String source,
        String channelId,
        String messageId,
        String attachmentUrl,
        String contentType
) {
    public static MediaRef avatar(String mediaId, String avatarUrl) {
        return new MediaRef(mediaId, "approved", 0, "discord_avatar", null, null, avatarUrl, "image");
    }

    public static MediaRef discordAttachment(
            String mediaId,
            String channelId,
            String messageId,
            String attachmentUrl,
            String contentType,
            int sortOrder
    ) {
        return new MediaRef(mediaId, "pending_review", sortOrder, "discord_attachment",
                channelId, messageId, attachmentUrl, contentType);
    }
}
