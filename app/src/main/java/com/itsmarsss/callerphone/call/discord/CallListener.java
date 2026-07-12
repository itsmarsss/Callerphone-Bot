package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public final class CallListener extends ListenerAdapter {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        Message message = event.getMessage();
        if (message.isWebhookMessage() || message.getAuthor().isBot() || message.getAuthor().isSystem()) {
            return;
        }
        String channelId = event.getChannel().getId();
        if (!calls.isInCall(channelId)) {
            return;
        }
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        if (!Users.hasUser(member.getId())) {
            ToolSet.sendPPAndTOS(event);
            return;
        }
        if (Users.isBlacklisted(member.getId())) {
            return;
        }
        String raw = message.getContentDisplay();
        if (raw.startsWith("\\\\") || raw.toLowerCase().startsWith(Callerphone.config.getPrefix().toLowerCase())) {
            return;
        }
        calls.handleMessage(channelId, message.getAuthor(), raw);
        if ((System.currentTimeMillis() - Cooldown.getCreditCooldown(event.getAuthor().getId())) > ToolSet.CREDIT_COOLDOWN) {
            Cooldown.setCreditCooldown(event.getAuthor().getId());
            Users.reward(event.getAuthor().getId(), 5);
            Users.addTransmit(event.getAuthor().getId(), 1);
        }
    }
}
