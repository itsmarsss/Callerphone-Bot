package com.itsmarsss.callerphone.call.model;

import java.time.Instant;
import java.util.List;

public record CallMessage(
        String authorId,
        String authorName,
        String content,
        String channelId,
        boolean fromSideA,
        List<String> flags,
        Instant sentAt
) {
}
