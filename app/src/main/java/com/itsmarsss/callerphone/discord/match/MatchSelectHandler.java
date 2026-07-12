package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.MatchConversationService;
import com.itsmarsss.callerphone.safety.ReportCategory;
import com.itsmarsss.commandType.IStringSelectInteraction;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.ArrayList;
import java.util.List;

public final class MatchSelectHandler implements IStringSelectInteraction {
    @Override
    public void runSelect(StringSelectInteractionEvent e) {
        if (!ApplicationContext.isReady()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("One moment", "Still starting up.")))
                    .setEphemeral(true).queue();
            return;
        }
        MatchComponentIds.Parsed parsed = MatchComponentIds.parse(e.getComponentId());
        if (parsed == null || e.getValues().isEmpty()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.expired())).setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String value = e.getValues().get(0);

        switch (parsed.action()) {
            case MatchComponentIds.ACTION_CHAT_MENU -> openChat(e, ctx, userId, value);
            case MatchComponentIds.ACTION_REPORT_CAT -> submitReport(e, ctx, userId, parsed.opaqueId(), value);
            default -> e.reply(ExperienceRenderer.toMessage(MatchPresenter.expired())).setEphemeral(true).queue();
        }
    }

    private void openChat(StringSelectInteractionEvent e, ApplicationContext ctx, String userId, String conversationId) {
        MatchConversationService.SelectResult result = ctx.conversations().select(userId, conversationId);
        if (!result.success()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("Chat", result.message())))
                    .setEphemeral(true).queue();
            return;
        }
        MatchConversation conversation = result.conversation();
        String other = conversation.otherParticipant(userId);
        String name = ctx.profiles().find(other).map(MatchProfile::getDisplayName).orElse("your connection");
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_STOP_CHAT, "_"), "Stop chat"));
        buttons.add(Button.danger(
                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_OPEN, "conversation:" + conversationId),
                "Safety"
        ));
        if (conversation.getStage() == ConversationStage.MEDIATED) {
            buttons.add(Button.primary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_REQUEST, conversationId),
                    "Request connect"
            ));
        } else if (conversation.getStage() == ConversationStage.CONNECT_PENDING
                && conversation.getConnectRequestedBy() != null
                && !conversation.getConnectRequestedBy().equals(userId)) {
            buttons.add(Button.success(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_ACCEPT, conversationId),
                    "Accept connect"
            ));
            buttons.add(Button.secondary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_DECLINE, conversationId),
                    "Decline"
            ));
        }
        e.replyEmbeds(MatchEmbeds.success("Chatting with " + name, result.message()))
                .addComponents(ActionRow.of(buttons))
                .setEphemeral(true)
                .queue();
    }

    private void submitReport(
            StringSelectInteractionEvent e,
            ApplicationContext ctx,
            String userId,
            String opaque,
            String categoryCode
    ) {
        MatchComponentIds.SafetyContext safety = MatchComponentIds.parseSafetyContext(opaque);
        String target = resolveTargetUserId(ctx, userId, safety);
        if (target == null) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
            return;
        }
        ReportCategory cat = ReportCategory.from(categoryCode).orElse(ReportCategory.OTHER);
        String evidenceType = safety != null && safety.kind() == MatchComponentIds.SafetyKind.CONVERSATION
                ? "conversation"
                : "user";
        String evidenceId = safety != null ? safety.referenceId() : target;
        ctx.safety().report(
                userId,
                target,
                cat.code(),
                cat.label(),
                evidenceType,
                evidenceId
        );
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyReported())).setEphemeral(true).queue();
    }

    private static String resolveTargetUserId(
            ApplicationContext ctx,
            String actorId,
            MatchComponentIds.SafetyContext safety
    ) {
        if (safety == null) {
            return null;
        }
        if (safety.kind() == MatchComponentIds.SafetyKind.PROFILE) {
            return safety.referenceId();
        }
        return ctx.conversations().find(safety.referenceId())
                .filter(c -> c.getParticipants() != null && c.getParticipants().contains(actorId))
                .map(c -> c.otherParticipant(actorId))
                .orElse(null);
    }

    @Override
    public String getID() {
        return "m";
    }
}
