package com.itsmarsss.callerphone.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private final ProductEventRepository events;

    public AnalyticsService(ProductEventRepository events) {
        this.events = events;
    }

    public void track(String userId, String name, String metadata) {
        try {
            events.append(ProductEvent.of(userId, name, metadata));
        } catch (Exception e) {
            logger.debug("Analytics write failed: {}", e.getMessage());
        }
    }

    /**
     * Plan §10.J — surface transitions: {@code surface:action} with optional reason/meta.
     */
    public void trackSurface(String userId, String surface, String action, String reason) {
        String name = (surface == null || surface.isBlank() ? "app" : surface)
                + "_" + (action == null || action.isBlank() ? "event" : action);
        String meta = reason == null ? "" : reason;
        track(userId, name, meta);
    }
}
