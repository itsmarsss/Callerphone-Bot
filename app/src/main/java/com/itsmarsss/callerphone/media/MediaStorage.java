package com.itsmarsss.callerphone.media;

import com.itsmarsss.callerphone.match.model.MediaRef;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Cheap beta storage: Discord attachment CDN via a private channel.
 * Alternative free-ish options (not implemented here):
 * - Discord avatars only (stable enough for MVP)
 * - Cloudflare R2 free tier / Backblaze B2 for durable blobs
 * - Base64 in Mongo (bad: size limits, backup bloat)
 *
 * Discord CDN URLs can expire; store channelId+messageId and re-resolve when needed.
 */
public interface MediaStorage {
    CompletableFuture<MediaRef> storeFromUrl(String ownerUserId, String sourceUrl, String contentType, int sortOrder);

    Optional<String> resolveUrl(MediaRef ref);
}
