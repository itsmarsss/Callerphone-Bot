package com.itsmarsss.callerphone.tccallerphone;

import com.itsmarsss.callerphone.tccallerphone.entities.ChatMode;
import com.itsmarsss.callerphone.tccallerphone.entities.Conversation;
import com.itsmarsss.callerphone.tccallerphone.services.ConversationService;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * Backward-compatible facade over {@link ConversationService}.
 * Prefer calling ConversationService directly in new code.
 */
public final class TCCallerphone {
    private static final ConversationService service = ConversationService.getInstance();

    private TCCallerphone() {
    }

    public static ChatStatus onCallCommand(MessageChannelUnion channel, ChatMode mode) {
        return service.startChat(channel.getId(), mode).getStatus();
    }

    public static MessageCreateData onEndCallCommand(MessageChannelUnion channel) {
        return service.endConversation(channel.getId());
    }

    public static void report(Conversation convo) {
        service.reportById(convo.getId());
    }

    public static Conversation getCall(String channelId) {
        return service.getActiveConversation(channelId).orElse(null);
    }

    public static boolean hasCall(String channelId) {
        return service.isInConversation(channelId);
    }
}
