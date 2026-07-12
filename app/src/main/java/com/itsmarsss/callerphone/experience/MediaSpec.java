package com.itsmarsss.callerphone.experience;

/** Optional image/thumbnail for an ExperienceView. */
public record MediaSpec(String thumbnailUrl, String imageUrl) {
    public static MediaSpec thumbnail(String url) {
        return new MediaSpec(url, null);
    }

    public static MediaSpec image(String url) {
        return new MediaSpec(null, url);
    }
}
