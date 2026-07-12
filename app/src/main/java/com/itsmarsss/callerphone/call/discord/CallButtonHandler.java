package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.service.CallProfileShareService;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public final class CallButtonHandler implements IButtonInteraction {
    private final CallSessionService sessions = CallSessionService.get();
    private final CallProfileShareService share = new CallProfileShareService(sessions);

    @Override
    public void runClick(ButtonInteraction e) {
        CallComponentIds.Parsed parsed = CallComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            if (e.getComponentId().startsWith("reportchat-")) {
                String id = e.getComponentId().substring("reportchat-".length());
                sessions.reportById(id);
                e.editButton(Button.danger("reportchat", "Reported").asDisabled()).queue();
                e.getMessage().replyEmbeds(CallEmbeds.success("Report received", "Thanks for reporting.")).queue();
                return;
            }
            e.replyEmbeds(CallEmbeds.warn("That expired", "Open a fresh screen to continue."))
                    .setEphemeral(true).queue();
            return;
        }
        wireShare();
        String userId = e.getUser().getId();
        String channelId = e.getChannel().getId();
        switch (parsed.action()) {
            case CallComponentIds.SHARE -> {
                var result = share.share(parsed.sessionId(), userId, channelId);
                e.replyEmbeds(result.success()
                                ? CallEmbeds.success("Profile shared", result.message())
                                : CallEmbeds.warn("Can't share", result.message()))
                        .setEphemeral(true).queue();
            }
            case CallComponentIds.LIKE -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), true, channelId);
                e.replyEmbeds(result.success()
                                ? CallEmbeds.success("Interest sent", result.message())
                                : CallEmbeds.warn("Couldn't send", result.message()))
                        .setEphemeral(true).queue();
            }
            case CallComponentIds.PASS -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), false, channelId);
                e.replyEmbeds(CallEmbeds.info("Noted", result.message())).setEphemeral(true).queue();
            }
            case CallComponentIds.REPORT -> {
                sessions.reportById(parsed.sessionId());
                e.editButton(Button.danger(CallComponentIds.report(parsed.sessionId()), "Reported").asDisabled()).queue();
                e.getMessage().replyEmbeds(CallEmbeds.success("Report received", "Thanks for reporting.")).queue();
            }
            default -> e.replyEmbeds(CallEmbeds.warn("That expired", "Open a fresh screen to continue."))
                    .setEphemeral(true).queue();
        }
    }

    private void wireShare() {
        if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
            share.setDecisions(com.itsmarsss.callerphone.bootstrap.ApplicationContext.get()
                    .decisionRepository());
        }
    }

    @Override
    public String getID() {
        return "c";
    }
}
