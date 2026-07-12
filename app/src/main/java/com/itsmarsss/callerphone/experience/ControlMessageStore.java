package com.itsmarsss.callerphone.experience;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks Discord control-message IDs so journeys can edit in place
 * (call lobby, discovery card, onboarding).
 *
 * Key format is presenter's responsibility, e.g. {@code call:lobby:{channelId}}
 * or {@code match:discover:{userId}}.
 */
public final class ControlMessageStore {
    private static final ControlMessageStore INSTANCE = new ControlMessageStore();

    private final ConcurrentHashMap<String, String> messageIds = new ConcurrentHashMap<>();

    private ControlMessageStore() {
    }

    public static ControlMessageStore get() {
        return INSTANCE;
    }

    public void put(String key, String messageId) {
        if (key == null || messageId == null || key.isBlank() || messageId.isBlank()) {
            return;
        }
        messageIds.put(key, messageId);
    }

    public Optional<String> get(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(messageIds.get(key));
    }

    public void remove(String key) {
        if (key != null) {
            messageIds.remove(key);
        }
    }

    public static String callLobbyKey(String channelId) {
        return "call:lobby:" + channelId;
    }

    public static String discoverKey(String userId) {
        return "match:discover:" + userId;
    }

    public static String onboardingKey(String userId) {
        return "match:onboard:" + userId;
    }
}
