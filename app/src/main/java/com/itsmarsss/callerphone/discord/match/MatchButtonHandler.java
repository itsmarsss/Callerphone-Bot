package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.DecisionType;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.service.DecisionService;
import com.itsmarsss.callerphone.match.service.DiscoveryService;
import com.itsmarsss.callerphone.match.service.MatchConversationService;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.util.ArrayList;
import java.util.List;

public final class MatchButtonHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        if (!ApplicationContext.isReady()) {
            e.replyEmbeds(MatchEmbeds.warm("One moment", "Still starting up.")).setEphemeral(true).queue();
            return;
        }
        MatchComponentIds.Parsed parsed = MatchComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            e.replyEmbeds(MatchEmbeds.warm("That expired", "Open a fresh screen to continue.")).setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String action = parsed.action();
        String opaque = parsed.opaqueId();

        switch (action) {
            case MatchComponentIds.ACTION_JOIN_ACCEPT -> {
                ctx.enrollment().acceptPolicies(userId);
                e.replyEmbeds(MatchEmbeds.soft(
                                "Age group",
                                "You'll only meet people in the same group."))
                        .addComponents(ActionRow.of(
                                Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_13_15, userId), "13-15"),
                                Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_16_17, userId), "16-17"),
                                Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_18_PLUS, userId), "18+")
                        ))
                        .setEphemeral(true)
                        .queue();
            }
            case MatchComponentIds.ACTION_AGE_13_15 -> selectAge(e, ctx, userId, AgeCohort.AGE_13_15);
            case MatchComponentIds.ACTION_AGE_16_17 -> selectAge(e, ctx, userId, AgeCohort.AGE_16_17);
            case MatchComponentIds.ACTION_AGE_18_PLUS -> selectAge(e, ctx, userId, AgeCohort.AGE_18_PLUS);
            case MatchComponentIds.ACTION_SETUP,
                 MatchComponentIds.ACTION_EDIT_BASICS,
                 MatchComponentIds.ACTION_EDIT_BIO,
                 MatchComponentIds.ACTION_EDIT_INTERESTS -> e.replyModal(MatchCommand.setupModal()).queue();
            case MatchComponentIds.ACTION_START_BROWSE -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> sendBrowse(e, ctx, userId));
            }
            case MatchComponentIds.ACTION_INTERESTED -> decide(e, ctx, userId, opaque, DecisionType.INTERESTED);
            case MatchComponentIds.ACTION_SKIP -> decide(e, ctx, userId, opaque, DecisionType.SKIP);
            case MatchComponentIds.ACTION_CHAT_SELECT -> replyChatSelect(e, ctx, userId, opaque);
            case MatchComponentIds.ACTION_CONNECT_REQUEST -> {
                EnrollmentService.ServiceResult result = ctx.connect().request(userId, opaque);
                e.replyEmbeds(result.success() ? MatchEmbeds.success("Done", result.message()) : MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_CONNECT_ACCEPT -> {
                var result = ctx.connect().accept(userId, opaque);
                if (result.success() && result.otherUserId() != null) {
                    e.replyEmbeds(MatchEmbeds.success("Connected", result.message() + "\n\nThey are <@" + result.otherUserId() + ">.")).setEphemeral(true).queue();
                } else {
                    e.replyEmbeds(result.success() ? MatchEmbeds.success("Done", result.message()) : MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
                }
            }
            case MatchComponentIds.ACTION_CONNECT_DECLINE -> {
                EnrollmentService.ServiceResult result = ctx.connect().decline(userId, opaque);
                e.replyEmbeds(result.success() ? MatchEmbeds.success("Done", result.message()) : MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_UNMATCH -> {
                EnrollmentService.ServiceResult result = ctx.conversations().unmatch(userId, opaque);
                e.replyEmbeds(result.success() ? MatchEmbeds.success("Done", result.message()) : MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_STOP_CHAT -> {
                EnrollmentService.ServiceResult result = ctx.conversations().stopChat(userId);
                e.replyEmbeds(result.success() ? MatchEmbeds.success("Done", result.message()) : MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_SUBMIT -> {
                ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
                EnrollmentService.ServiceResult result = ctx.profiles().publish(userId);
                if (result.success()) {
                    e.replyEmbeds(MatchEmbeds.success("You're live", result.message()))
                            .addComponents(ActionRow.of(
                                    Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover")
                            ))
                            .setEphemeral(true)
                            .queue();
                } else {
                    e.replyEmbeds(MatchEmbeds.warm("Almost", result.message()))
                            .addComponents(ActionRow.of(
                                    Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Finish setup")
                            ))
                            .setEphemeral(true)
                            .queue();
                }
            }
            case MatchComponentIds.ACTION_BROWSE_NEXT -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> sendBrowse(e, ctx, userId));
            }
            default -> e.replyEmbeds(MatchEmbeds.warm("That expired", "Open a fresh screen to continue.")).setEphemeral(true).queue();
        }
    }

    private void selectAge(ButtonInteraction e, ApplicationContext ctx, String userId, AgeCohort cohort) {
        EnrollmentService.ServiceResult result = ctx.enrollment().selectAgeCohort(userId, cohort);
        ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
        if (!result.success()) {
            e.replyEmbeds(MatchEmbeds.warm("Couldn't continue", result.message())).setEphemeral(true).queue();
            return;
        }
        // Lowest friction: open the one setup form immediately (no extra commands)
        e.replyModal(MatchCommand.setupModal()).queue();
    }

    private void replyChatSelect(ButtonInteraction e, ApplicationContext ctx, String userId, String conversationId) {
        MatchConversationService.SelectResult result = ctx.conversations().select(userId, conversationId);
        if (!result.success()) {
            e.replyEmbeds(result.success() ? MatchEmbeds.success("Done", result.message()) : MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
            return;
        }
        MatchConversation conversation = result.conversation();
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_STOP_CHAT, "_"), "Stop chat"));
        buttons.add(Button.danger(MatchComponentIds.of(MatchComponentIds.ACTION_UNMATCH, conversationId), "Unmatch"));
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
        e.replyEmbeds(MatchEmbeds.simple("Chat ready", result.message()))
                .addComponents(ActionRow.of(buttons))
                .setEphemeral(true)
                .queue();
    }

    private void decide(ButtonInteraction e, ApplicationContext ctx, String userId, String sessionId, DecisionType type) {
        e.deferEdit().queue();
        ctx.dbExecutor().execute(() -> {
            DecisionService.DecisionResult result = ctx.decisions().decide(userId, sessionId, type);
            if (!result.success()) {
                e.getHook().sendMessageEmbeds(MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
                return;
            }
            String note = result.message();
            if (result.mutual() && result.conversationId() != null) {
                e.getHook().editOriginalEmbeds(MatchEmbeds.success("You connected", note))
                        .setContent(null)
                        .setComponents(ActionRow.of(
                                Button.success(
                                        MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, result.conversationId()),
                                        "Open chat"
                                )
                        ))
                        .queue();
                return;
            }
            DiscoveryService.DiscoveryResult next = ctx.discovery().next(userId);
            if (!next.success()) {
                e.getHook().editOriginalEmbeds(MatchEmbeds.soft(note, next.message()))
                        .setContent(null)
                        .setComponents(List.of())
                        .queue();
                return;
            }
            e.getHook().editOriginalEmbeds(MatchEmbeds.profileCard(next.profile(), false))
                    .setContent(note)
                    .setComponents(ActionRow.of(
                            Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, next.session().sessionId()), "Interested"),
                            Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, next.session().sessionId()), "Next")
                    ))
                    .queue();
        });
    }

    private void sendBrowse(ButtonInteraction e, ApplicationContext ctx, String userId) {
        DiscoveryService.DiscoveryResult next = ctx.discovery().next(userId);
        if (!next.success()) {
            e.getHook().sendMessageEmbeds(MatchEmbeds.soft("Discover", next.message())).setEphemeral(true).queue();
            return;
        }
        e.getHook().sendMessageEmbeds(MatchEmbeds.profileCard(next.profile(), false))
                .addComponents(ActionRow.of(
                        Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, next.session().sessionId()), "Interested"),
                        Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, next.session().sessionId()), "Next")
                ))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public String getID() {
        return "m";
    }
}
