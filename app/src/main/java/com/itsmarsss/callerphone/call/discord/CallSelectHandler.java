package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.safety.ReportCategory;
import com.itsmarsss.commandType.IStringSelectInteraction;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public final class CallSelectHandler implements IStringSelectInteraction {
    private final CallSessionService sessions = CallSessionService.get();

    @Override
    public void runSelect(StringSelectInteractionEvent e) {
        CallComponentIds.Parsed parsed = CallComponentIds.parse(e.getComponentId());
        if (parsed == null || e.getValues().isEmpty()) {
            e.reply(ExperienceRenderer.toMessage(CallPresenter.warn(
                    "That expired",
                    "Open a fresh report screen to continue."
            ))).setEphemeral(true).queue();
            return;
        }
        if (!CallComponentIds.REPORT_CAT.equals(parsed.action())) {
            e.reply(ExperienceRenderer.toMessage(CallPresenter.warn(
                    "That expired",
                    "Open a fresh screen to continue."
            ))).setEphemeral(true).queue();
            return;
        }
        String sessionId = parsed.sessionId();
        ReportCategory cat = ReportCategory.from(e.getValues().get(0)).orElse(ReportCategory.OTHER);
        sessions.reportById(sessionId);
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(e.getUser().getId(), "call_report", cat.code());
            }
        } catch (Exception ignored) {
        }
        e.reply(ExperienceRenderer.toMessage(CallPresenter.success(
                "Report received",
                "Category: **" + cat.label() + "**. Recent transcript was preserved for review."
        ))).setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "c";
    }
}
