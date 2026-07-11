package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.service.CallProfileShareService;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public final class CallButtonHandler implements IButtonInteraction {
    private final CallSessionService sessions = CallSessionService.get();
    private final CallProfileShareService share = new CallProfileShareService(sessions);

    public CallButtonHandler() {
        // decisions wired lazily when ApplicationContext is ready
    }

    @Override
    public void runClick(ButtonInteraction e) {
        CallComponentIds.Parsed parsed = CallComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            // legacy reportchat-id
            if (e.getComponentId().startsWith("reportchat-")) {
                String id = e.getComponentId().substring("reportchat-".length());
                sessions.reportById(id);
                e.editButton(Button.danger("reportchat", "Reported").asDisabled()).queue();
                e.getMessage().reply(ToolSet.CP_EMJ + " Call reported.").queue();
                return;
            }
            e.reply(ToolSet.CP_EMJ + " Unknown call button.").setEphemeral(true).queue();
            return;
        }
        wireShare();
        String userId = e.getUser().getId();
        String channelId = e.getChannel().getId();
        switch (parsed.action()) {
            case CallComponentIds.SHARE -> {
                var result = share.share(parsed.sessionId(), userId, channelId);
                e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
            }
            case CallComponentIds.LIKE -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), true, channelId);
                e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
            }
            case CallComponentIds.PASS -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), false, channelId);
                e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
            }
            case CallComponentIds.REPORT -> {
                sessions.reportById(parsed.sessionId());
                e.editButton(Button.danger(CallComponentIds.report(parsed.sessionId()), "Reported").asDisabled()).queue();
                e.getMessage().reply(ToolSet.CP_EMJ + " Call reported to staff.").queue();
            }
            default -> e.reply(ToolSet.CP_EMJ + " Unknown call action.").setEphemeral(true).queue();
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
