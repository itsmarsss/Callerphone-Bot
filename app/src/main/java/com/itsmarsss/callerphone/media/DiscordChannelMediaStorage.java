package com.itsmarsss.callerphone.media;

import com.itsmarsss.callerphone.match.model.MediaRef;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Re-hosts images in a private Discord channel and stores message/attachment refs.
 * Configure {@code matchMediaChannel} in config.yml.
 */
public final class DiscordChannelMediaStorage implements MediaStorage {
    private static final Logger logger = LoggerFactory.getLogger(DiscordChannelMediaStorage.class);

    private final JDA jda;
    private final String mediaChannelId;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public DiscordChannelMediaStorage(JDA jda, String mediaChannelId) {
        this.jda = jda;
        this.mediaChannelId = mediaChannelId == null ? "" : mediaChannelId;
    }

    @Override
    public CompletableFuture<MediaRef> storeFromUrl(String ownerUserId, String sourceUrl, String contentType, int sortOrder) {
        if (mediaChannelId.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalStateException("matchMediaChannel not configured"));
        }
        TextChannel channel = jda.getTextChannelById(mediaChannelId);
        if (channel == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Media channel not found"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl))
                        .timeout(Duration.ofSeconds(20))
                        .GET()
                        .build();
                HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() >= 400) {
                    throw new IllegalStateException("Download failed: HTTP " + response.statusCode());
                }
                String filename = "match-" + ownerUserId + "-" + UUID.randomUUID() + ".jpg";
                try (InputStream body = response.body()) {
                    Message message = channel.sendFiles(FileUpload.fromData(body.readAllBytes(), filename))
                            .setContent("owner=" + ownerUserId)
                            .complete();
                    String url = message.getAttachments().isEmpty()
                            ? null
                            : message.getAttachments().get(0).getUrl();
                    return MediaRef.discordAttachment(
                            UUID.randomUUID().toString(),
                            channel.getId(),
                            message.getId(),
                            url,
                            contentType == null ? "image" : contentType,
                            sortOrder
                    );
                }
            } catch (Exception e) {
                logger.error("Failed to store media for {}", ownerUserId, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Optional<String> resolveUrl(MediaRef ref) {
        if (ref == null) {
            return Optional.empty();
        }
        if (ref.attachmentUrl() != null && !ref.attachmentUrl().isBlank()) {
            return Optional.of(ref.attachmentUrl());
        }
        if (ref.channelId() == null || ref.messageId() == null) {
            return Optional.empty();
        }
        try {
            TextChannel channel = jda.getTextChannelById(ref.channelId());
            if (channel == null) {
                return Optional.empty();
            }
            Message message = channel.retrieveMessageById(ref.messageId()).complete();
            if (message.getAttachments().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(message.getAttachments().get(0).getUrl());
        } catch (Exception e) {
            logger.warn("Could not resolve media {}", ref.mediaId(), e);
            return Optional.empty();
        }
    }
}
