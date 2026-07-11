package com.itsmarsss.callerphone.safety;

import java.time.Instant;

public record Block(
        String blockerId,
        String blockedId,
        String product,
        Instant createdAt,
        String reason
) {
    public static final String PRODUCT_MATCH = "match";
    public static final String PRODUCT_CALL = "call";
    public static final String PRODUCT_GLOBAL = "global";

    public static Block match(String blockerId, String blockedId, String reason) {
        return new Block(blockerId, blockedId, PRODUCT_MATCH, Instant.now(), reason == null ? "" : reason);
    }
}
