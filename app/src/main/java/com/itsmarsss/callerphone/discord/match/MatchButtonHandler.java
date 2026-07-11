package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.DecisionType;
import com.itsmarsss.callerphone.match.service.DecisionService;
import com.itsmarsss.callerphone.match.service.DiscoveryService;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.util.List;

public final class MatchButtonHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        if (!ApplicationContext.isReady()) {
            e.reply(ToolSet.CP_EMJ + " Match is starting up.").setEphemeral(true).queue();
            return;
        }
        MatchComponentIds.Parsed parsed = MatchComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            e.reply(ToolSet.CP_EMJ + " Unknown Match button.").setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String action = parsed.action();
        String opaque = parsed.opaqueId();

        switch (action) {
            case MatchComponentIds.ACTION_JOIN_ACCEPT -> {
                EnrollmentService.ServiceResult accepted = ctx.enrollment().acceptPolicies(userId);
                e.replyEmbeds(MatchEmbeds.simple("Choose your age group",
                                accepted.message() + "\n\nThis is **only** used to pair you with the same group. Groups never mix."))
                        .addComponents(ActionRow.of(
                                Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_13_15, userId), "13–15"),
                                Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_16_17, userId), "16–17"),
                                Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_18_PLUS, userId), "18+")
                        ))
                        .setEphemeral(true)
                        .queue();
            }
            case MatchComponentIds.ACTION_AGE_13_15 -> selectAge(e, ctx, userId, AgeCohort.AGE_13_15);
            case MatchComponentIds.ACTION_AGE_16_17 -> selectAge(e, ctx, userId, AgeCohort.AGE_16_17);
            case MatchComponentIds.ACTION_AGE_18_PLUS -> selectAge(e, ctx, userId, AgeCohort.AGE_18_PLUS);
            case MatchComponentIds.ACTION_INTERESTED -> decide(e, ctx, userId, opaque, DecisionType.INTERESTED);
            case MatchComponentIds.ACTION_SKIP -> decide(e, ctx, userId, opaque, DecisionType.SKIP);
            case MatchComponentIds.ACTION_CHAT_SELECT -> {
                EnrollmentService.ServiceResult result = ctx.conversations().select(userId, opaque);
                e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_CONNECT_ACCEPT -> {
                var result = ctx.connect().accept(userId, opaque);
                if (result.success() && result.otherUserId() != null) {
                    e.reply(ToolSet.CP_EMJ + " " + result.message() + " Other user: <@" + result.otherUserId() + ">")
                            .setEphemeral(true).queue();
                } else {
                    e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
                }
            }
            case MatchComponentIds.ACTION_CONNECT_DECLINE -> {
                EnrollmentService.ServiceResult result = ctx.connect().decline(userId, opaque);
                e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_EDIT_BASICS -> e.replyModal(MatchCommand.basicsModal()).queue();
            case MatchComponentIds.ACTION_EDIT_BIO -> e.replyModal(MatchCommand.bioModal()).queue();
            case MatchComponentIds.ACTION_EDIT_INTERESTS -> e.replyModal(MatchCommand.interestsModal()).queue();
            case MatchComponentIds.ACTION_BROWSE_NEXT -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> sendBrowse(e, ctx, userId));
            }
            default -> e.reply(ToolSet.CP_EMJ + " That Match action expired or is unknown.").setEphemeral(true).queue();
        }
    }

    private void selectAge(ButtonInteraction e, ApplicationContext ctx, String userId, AgeCohort cohort) {
        EnrollmentService.ServiceResult result = ctx.enrollment().selectAgeCohort(userId, cohort);
        ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
        e.replyEmbeds(MatchEmbeds.simple(
                "Age group saved",
                result.message() + "\n\nNext: `/match edit field:basics`, then bio, interests, then `/match submit`."
        )).setEphemeral(true).queue();
    }

    private void decide(ButtonInteraction e, ApplicationContext ctx, String userId, String sessionId, DecisionType type) {
        e.deferEdit().queue();
        ctx.dbExecutor().execute(() -> {
            DecisionService.DecisionResult result = ctx.decisions().decide(userId, sessionId, type);
            if (!result.success()) {
                e.getHook().sendMessage(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
                return;
            }
            String note = result.message();
            DiscoveryService.DiscoveryResult next = ctx.discovery().next(userId);
            if (!next.success()) {
                e.getHook().editOriginal(ToolSet.CP_EMJ + " " + note + "\n" + next.message())
                        .setEmbeds()
                        .setComponents(List.of())
                        .queue();
                return;
            }
            e.getHook().editOriginalEmbeds(MatchEmbeds.profileCard(next.profile(), false))
                    .setContent(ToolSet.CP_EMJ + " " + note)
                    .setComponents(ActionRow.of(
                            Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, next.session().sessionId()), "Interested"),
                            Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, next.session().sessionId()), "Skip")
                    ))
                    .queue();
        });
    }

    private void sendBrowse(ButtonInteraction e, ApplicationContext ctx, String userId) {
        DiscoveryService.DiscoveryResult next = ctx.discovery().next(userId);
        if (!next.success()) {
            e.getHook().sendMessage(ToolSet.CP_EMJ + " " + next.message()).setEphemeral(true).queue();
            return;
        }
        e.getHook().sendMessageEmbeds(MatchEmbeds.profileCard(next.profile(), false))
                .addComponents(ActionRow.of(
                        Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, next.session().sessionId()), "Interested"),
                        Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, next.session().sessionId()), "Skip")
                ))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public String getID() {
        return "m";
    }
}
