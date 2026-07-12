package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.ConversationStage;
import com.itsmarsss.callerphone.match.model.DecisionType;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.DecisionService;
import com.itsmarsss.callerphone.match.service.MatchConversationService;
import com.itsmarsss.callerphone.safety.ReportCategory;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MatchButtonHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        if (!ApplicationContext.isReady()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("One moment", "Still starting up.")))
                    .setEphemeral(true).queue();
            return;
        }
        MatchComponentIds.Parsed parsed = MatchComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.expired())).setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String action = parsed.action();
        String opaque = parsed.opaqueId();

        switch (action) {
            case MatchComponentIds.ACTION_JOIN_ACCEPT -> {
                ctx.enrollment().acceptPolicies(userId);
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.ageGroup()))
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
                ctx.dbExecutor().execute(() -> DiscoveryUi.sendDiscover(e.getHook(), ctx, userId, true));
            }
            case MatchComponentIds.ACTION_OPEN_CHATS -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> ChatInboxUi.sendInbox(e.getHook(), ctx, userId));
            }
            case MatchComponentIds.ACTION_INTERESTED -> decide(e, ctx, userId, opaque, DecisionType.INTERESTED);
            case MatchComponentIds.ACTION_SKIP -> decide(e, ctx, userId, opaque, DecisionType.SKIP);
            case MatchComponentIds.ACTION_CHAT_SELECT -> replyChatSelect(e, ctx, userId, opaque);
            case MatchComponentIds.ACTION_CONNECT_REQUEST -> {
                EnrollmentService.ServiceResult result = ctx.connect().request(userId, opaque);
                replyService(e, result);
            }
            case MatchComponentIds.ACTION_CONNECT_ACCEPT -> {
                var result = ctx.connect().accept(userId, opaque);
                if (result.success() && result.otherUserId() != null) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.quietSuccess(
                            "Connected",
                            result.message() + "\n\nThey are <@" + result.otherUserId() + ">."
                    ))).setEphemeral(true).queue();
                } else {
                    e.reply(ExperienceRenderer.toMessage(result.success()
                            ? MatchPresenter.quietSuccess("Connected", result.message())
                            : MatchPresenter.warn("Couldn't complete", result.message())
                    )).setEphemeral(true).queue();
                }
            }
            case MatchComponentIds.ACTION_CONNECT_DECLINE -> replyService(e, ctx.connect().decline(userId, opaque));
            case MatchComponentIds.ACTION_UNMATCH -> replyService(e, ctx.conversations().unmatch(userId, opaque));
            case MatchComponentIds.ACTION_STOP_CHAT -> replyService(e, ctx.conversations().stopChat(userId));
            case MatchComponentIds.ACTION_SUBMIT -> {
                ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
                EnrollmentService.ServiceResult result = ctx.profiles().publish(userId);
                if (result.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.liveReady())).setEphemeral(true).queue();
                } else {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.setupRetry(result.message())))
                            .setEphemeral(true).queue();
                }
            }
            case MatchComponentIds.ACTION_BROWSE_NEXT -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> DiscoveryUi.sendDiscover(e.getHook(), ctx, userId, true));
            }
            case MatchComponentIds.ACTION_SAFETY_OPEN -> openSafety(e, ctx, userId, opaque);
            case MatchComponentIds.ACTION_SAFETY_BLOCK -> safetyBlock(e, ctx, userId, opaque);
            case MatchComponentIds.ACTION_SAFETY_REPORT -> safetyReport(e, ctx, userId, opaque);
            case MatchComponentIds.ACTION_SAFETY_UNMATCH -> safetyUnmatch(e, ctx, userId, opaque);
            case MatchComponentIds.ACTION_LEAVE_CONFIRM -> replyService(e, ctx.deletion().leaveAndSoftDelete(userId));
            case MatchComponentIds.ACTION_LEAVE_CANCEL -> e.reply(ExperienceRenderer.toMessage(
                            MatchPresenter.quietSuccess("Still live", "You're still in Discover.")
                    )).setEphemeral(true).queue();
            case MatchComponentIds.ACTION_DELETE_CONFIRM -> replyService(e, ctx.deletion().hardDeleteProfileContent(userId));
            case MatchComponentIds.ACTION_DELETE_CANCEL -> e.reply(ExperienceRenderer.toMessage(
                            MatchPresenter.quietSuccess("Cancelled", "Nothing was deleted.")
                    )).setEphemeral(true).queue();
            case MatchComponentIds.ACTION_PREVIEW_SELF -> {
                Optional<MatchProfile> p = ctx.profiles().find(userId);
                if (p.isEmpty()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("No profile", "Start with `/match join`.")))
                            .setEphemeral(true).queue();
                } else {
                    e.replyEmbeds(MatchEmbeds.profileCard(p.get(), true)).setEphemeral(true).queue();
                }
            }
            case MatchComponentIds.ACTION_EDIT_MENU -> e.reply(ExperienceRenderer.toMessage(MatchPresenter.editMenu()))
                    .setEphemeral(true).queue();
            case MatchComponentIds.ACTION_SETTINGS -> {
                var user = ctx.enrollment().getOrCreate(userId);
                e.reply(ExperienceRenderer.toMessage(
                        MatchPresenter.settings(user.isNotificationsEnabled(), user.isDigestOptIn())
                )).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_PHOTO_MENU -> e.reply(ExperienceRenderer.toMessage(MatchPresenter.photoMenu()))
                    .setEphemeral(true).queue();
            case MatchComponentIds.ACTION_PHOTO_AVATAR -> {
                var r = ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
                if (r.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.photoUpdated())).setEphemeral(true).queue();
                } else {
                    replyService(e, r);
                }
            }
            case MatchComponentIds.ACTION_PREMIUM -> e.reply(ExperienceRenderer.toMessage(
                    MatchPresenter.premiumOverview(ctx.premium().isPremium(userId))
            )).setEphemeral(true).queue();
            case MatchComponentIds.ACTION_TOGGLE_NOTIFY -> {
                var user = ctx.enrollment().getOrCreate(userId);
                boolean next = !user.isNotificationsEnabled();
                var r = ctx.enrollment().setNotifications(userId, next);
                user = ctx.enrollment().getOrCreate(userId);
                e.reply(ExperienceRenderer.toMessage(r.success()
                        ? MatchPresenter.settings(user.isNotificationsEnabled(), user.isDigestOptIn())
                        : MatchPresenter.warn("Couldn't update", r.message())
                )).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_TOGGLE_DIGEST -> {
                var user = ctx.enrollment().getOrCreate(userId);
                boolean next = !user.isDigestOptIn();
                var r = ctx.enrollment().setDigestOptIn(userId, next);
                user = ctx.enrollment().getOrCreate(userId);
                e.reply(ExperienceRenderer.toMessage(r.success()
                        ? MatchPresenter.settings(user.isNotificationsEnabled(), user.isDigestOptIn())
                        : MatchPresenter.warn("Couldn't update", r.message())
                )).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_RESUME -> replyService(e, ctx.profiles().resume(userId));
            case MatchComponentIds.ACTION_OPEN_LIKES -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> {
                    var incoming = ctx.incomingLikes(userId);
                    if (incoming.isEmpty()) {
                        e.getHook().sendMessage(ExperienceRenderer.toMessage(MatchPresenter.incomingInterestEmpty()))
                                .setEphemeral(true).queue();
                        return;
                    }
                    if (!ctx.premium().canSeeIncomingInterestNames(userId)) {
                        e.getHook().sendMessage(ExperienceRenderer.toMessage(MatchPresenter.incomingInterestFreeTeaser()))
                                .setEphemeral(true).queue();
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    int i = 1;
                    for (var d : incoming) {
                        String name = ctx.profiles().find(d.viewerId()).map(MatchProfile::getDisplayName).orElse("Someone");
                        sb.append("**").append(i++).append(".** ").append(name).append("\n");
                        if (i > 15) {
                            break;
                        }
                    }
                    e.getHook().sendMessage(ExperienceRenderer.toMessage(
                                    MatchPresenter.incomingInterestList(sb + "\nDiscover to express interest back.")
                            ))
                            .setEphemeral(true).queue();
                });
            }
            case MatchComponentIds.ACTION_HOME,
                 MatchComponentIds.ACTION_BACK_INBOX -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> InboxUi.send(e.getHook(), ctx, userId));
            }
            case MatchComponentIds.ACTION_INBOX_OPEN -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() ->
                        e.getHook().sendMessage(InboxUi.openNext(ctx, userId)).setEphemeral(true).queue()
                );
            }
            case MatchComponentIds.ACTION_INBOX_READ_ALL -> {
                ctx.inbox().markAllRead(userId);
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.quietSuccess(
                        "Inbox cleared",
                        "Non-safety updates marked read. Open next anytime from your inbox."
                ))).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_GAME_TTT -> {
                var result = ctx.connectionGames().proposeTtt(opaque, userId);
                e.reply(ExperienceRenderer.toMessage(result.success()
                        ? MatchPresenter.quietSuccess("Challenge sent", result.message())
                        : MatchPresenter.warn("Couldn't challenge", result.message())
                )).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_GAME_ACCEPT -> {
                String proposerId = ctx.connectionGames().pendingProposer(opaque).orElse(null);
                if (proposerId == null) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn(
                            "No challenge",
                            "There's no pending game for this chat."
                    ))).setEphemeral(true).queue();
                    return;
                }
                e.getJDA().retrieveUserById(proposerId).queue(
                        proposer -> {
                            var result = ctx.connectionGames().acceptTtt(
                                    opaque, userId, e.getUser(), proposer
                            );
                            e.reply(ExperienceRenderer.toMessage(result.success()
                                    ? MatchPresenter.quietSuccess("Game on", result.message())
                                    : MatchPresenter.warn("Couldn't start", result.message())
                            )).setEphemeral(true).queue();
                        },
                        err -> e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn(
                                "Couldn't start",
                                "Couldn't resolve the other player."
                        ))).setEphemeral(true).queue()
                );
            }
            case MatchComponentIds.ACTION_GAME_DECLINE -> {
                var result = ctx.connectionGames().decline(opaque, userId);
                e.reply(ExperienceRenderer.toMessage(result.success()
                        ? MatchPresenter.quietSuccess("Declined", result.message())
                        : MatchPresenter.warn("Couldn't decline", result.message())
                )).setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_BOTTLE_INTEREST -> {
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> {
                    var result = ctx.decisions().expressInterest(userId, opaque);
                    try {
                        ctx.analytics().track(userId, "bottle_thread_to_interest",
                                result.mutual() ? "mutual" : (result.success() ? "sent" : "fail"));
                    } catch (Exception ignored) {
                    }
                    if (result.mutual() && result.conversationId() != null) {
                        String peerName = "your connection";
                        if (result.match() != null) {
                            String peerId = result.match().otherUserId(userId);
                            if (peerId != null) {
                                peerName = ctx.profiles().find(peerId)
                                        .map(MatchProfile::getDisplayName).orElse(peerName);
                            }
                        }
                        e.getHook().sendMessage(ExperienceRenderer.toMessage(
                                MatchPresenter.connected(peerName, null, result.conversationId())
                        )).setEphemeral(true).queue();
                        return;
                    }
                    e.getHook().sendMessage(ExperienceRenderer.toMessage(result.success()
                            ? MatchPresenter.quietSuccess("Interest sent", result.message())
                            : MatchPresenter.warn("Couldn't send interest", result.message())
                    )).setEphemeral(true).queue();
                });
            }
            default -> e.reply(ExperienceRenderer.toMessage(MatchPresenter.expired())).setEphemeral(true).queue();
        }
    }

    private static void replyService(ButtonInteraction e, EnrollmentService.ServiceResult result) {
        e.reply(ExperienceRenderer.toMessage(result.success()
                ? MatchPresenter.quietSuccess("Done", result.message())
                : MatchPresenter.warn("Couldn't complete", result.message())
        )).setEphemeral(true).queue();
    }

    private void selectAge(ButtonInteraction e, ApplicationContext ctx, String userId, AgeCohort cohort) {
        EnrollmentService.ServiceResult result = ctx.enrollment().selectAgeCohort(userId, cohort);
        ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
        if (!result.success()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("Couldn't continue", result.message())))
                    .setEphemeral(true).queue();
            return;
        }
        e.replyModal(MatchCommand.setupModal()).queue();
    }

    private void replyChatSelect(ButtonInteraction e, ApplicationContext ctx, String userId, String conversationId) {
        MatchConversationService.SelectResult result = ctx.conversations().select(userId, conversationId);
        if (!result.success()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("Chat", result.message())))
                    .setEphemeral(true).queue();
            return;
        }
        MatchConversation conversation = result.conversation();
        String other = conversation.otherParticipant(userId);
        String name = ctx.profiles().find(other).map(MatchProfile::getDisplayName).orElse("your connection");
        List<Button> row1 = new ArrayList<>();
        List<Button> row2 = new ArrayList<>();
        row1.add(Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_STOP_CHAT, "_"), "Stop chat"));
        row1.add(Button.danger(
                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_OPEN, "conversation:" + conversationId),
                "Safety"
        ));
        Optional<String> pendingGame = ctx.connectionGames().pendingProposer(conversationId);
        if (pendingGame.isPresent() && !pendingGame.get().equals(userId)) {
            row2.add(Button.success(
                    MatchComponentIds.of(MatchComponentIds.ACTION_GAME_ACCEPT, conversationId),
                    "Play Tic-Tac-Toe"
            ));
            row2.add(Button.secondary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_GAME_DECLINE, conversationId),
                    "Decline game"
            ));
        } else if (pendingGame.isPresent() && pendingGame.get().equals(userId)) {
            row2.add(Button.primary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_GAME_TTT, conversationId),
                    "Waiting…"
            ).asDisabled());
            row2.add(Button.secondary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_GAME_DECLINE, conversationId),
                    "Cancel game"
            ));
        } else {
            row2.add(Button.primary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_GAME_TTT, conversationId),
                    "Play a game"
            ));
        }
        if (conversation.getStage() == ConversationStage.MEDIATED) {
            row1.add(Button.primary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_REQUEST, conversationId),
                    "Request connect"
            ));
        } else if (conversation.getStage() == ConversationStage.CONNECT_PENDING
                && conversation.getConnectRequestedBy() != null
                && !conversation.getConnectRequestedBy().equals(userId)) {
            row1.add(Button.success(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_ACCEPT, conversationId),
                    "Accept connect"
            ));
            row1.add(Button.secondary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CONNECT_DECLINE, conversationId),
                    "Decline"
            ));
        }
        var reply = e.replyEmbeds(MatchEmbeds.success("Chatting with " + name, result.message()))
                .addComponents(ActionRow.of(row1));
        if (!row2.isEmpty()) {
            reply = reply.addComponents(ActionRow.of(row2));
        }
        reply.setEphemeral(true).queue();
    }

    private void decide(ButtonInteraction e, ApplicationContext ctx, String userId, String sessionId, DecisionType type) {
        e.deferEdit().queue();
        ctx.dbExecutor().execute(() -> {
            DecisionService.DecisionResult result = ctx.decisions().decide(userId, sessionId, type);
            if (!result.success()) {
                String msg = result.message() == null ? "" : result.message();
                String lower = msg.toLowerCase();
                if (lower.contains("interests used") || lower.contains("chat limit") || lower.contains("limit reached")) {
                    e.getHook().sendMessage(ExperienceRenderer.toMessage(
                            MatchPresenter.softLimit("Not right now", msg)
                    )).setEphemeral(true).queue();
                    return;
                }
                e.getHook().sendMessage(ExperienceRenderer.toMessage(
                        MatchPresenter.warn("Couldn't complete", result.message())
                )).setEphemeral(true).queue();
                return;
            }
            if (result.mutual() && result.conversationId() != null) {
                String peerName = "your connection";
                String opener = null;
                if (result.match() != null) {
                    String peerId = result.match().otherUserId(userId);
                    if (peerId != null) {
                        peerName = ctx.profiles().find(peerId).map(MatchProfile::getDisplayName).orElse(peerName);
                        opener = com.itsmarsss.callerphone.match.service.Icebreakers.forPair(
                                ctx.profiles().find(userId).orElse(null),
                                ctx.profiles().find(peerId).orElse(null)
                        );
                    }
                }
                e.getHook().editOriginal(ExperienceRenderer.toEdit(
                        MatchPresenter.connected(peerName, opener, result.conversationId())
                )).queue();
                return;
            }
            String note = result.message() == null || result.message().isBlank()
                    ? (type == DecisionType.INTERESTED ? "Interest sent privately" : "Next")
                    : result.message();
            DiscoveryUi.editDiscoverCard(e.getHook(), ctx, userId, note);
        });
    }

    private void openSafety(ButtonInteraction e, ApplicationContext ctx, String userId, String opaque) {
        MatchComponentIds.SafetyContext safety = MatchComponentIds.parseSafetyContext(opaque);
        if (safety == null) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
            return;
        }
        String display = resolveSafetyName(ctx, userId, safety);
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyMenu(display, safety.opaque())))
                .setEphemeral(true).queue();
    }

    private void safetyBlock(ButtonInteraction e, ApplicationContext ctx, String userId, String opaque) {
        MatchComponentIds.SafetyContext safety = MatchComponentIds.parseSafetyContext(opaque);
        String target = resolveTargetUserId(ctx, userId, safety);
        if (target == null) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
            return;
        }
        ctx.safety().block(userId, target, "user_block");
        if (safety != null && safety.kind() == MatchComponentIds.SafetyKind.CONVERSATION) {
            ctx.conversations().unmatch(userId, safety.referenceId());
        }
        String name = ctx.profiles().find(target).map(MatchProfile::getDisplayName).orElse("That person");
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyBlocked(name))).setEphemeral(true).queue();
    }

    private void safetyReport(ButtonInteraction e, ApplicationContext ctx, String userId, String opaque) {
        MatchComponentIds.SafetyContext safety = MatchComponentIds.parseSafetyContext(opaque);
        String target = resolveTargetUserId(ctx, userId, safety);
        if (target == null) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
            return;
        }
        StringSelectMenu.Builder menu = StringSelectMenu.create(
                        MatchComponentIds.of(MatchComponentIds.ACTION_REPORT_CAT, opaque))
                .setPlaceholder("Choose a reason")
                .setRequiredRange(1, 1);
        for (ReportCategory cat : ReportCategory.values()) {
            menu.addOption(cat.label(), cat.code());
        }
        e.replyEmbeds(MatchEmbeds.soft(
                        "Report",
                        "Choose the closest reason. Evidence from this chat or profile is attached."
                ))
                .addComponents(ActionRow.of(menu.build()))
                .setEphemeral(true)
                .queue();
    }

    private void safetyUnmatch(ButtonInteraction e, ApplicationContext ctx, String userId, String opaque) {
        MatchComponentIds.SafetyContext safety = MatchComponentIds.parseSafetyContext(opaque);
        if (safety == null || safety.kind() != MatchComponentIds.SafetyKind.CONVERSATION) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn(
                    "Unmatch",
                    "Open Safety from a chat to unmatch that connection."
            ))).setEphemeral(true).queue();
            return;
        }
        replyService(e, ctx.conversations().unmatch(userId, safety.referenceId()));
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

    private static String resolveSafetyName(
            ApplicationContext ctx,
            String actorId,
            MatchComponentIds.SafetyContext safety
    ) {
        String target = resolveTargetUserId(ctx, actorId, safety);
        if (target == null) {
            return "this person";
        }
        return ctx.profiles().find(target).map(MatchProfile::getDisplayName).orElse("this person");
    }

    @Override
    public String getID() {
        return "m";
    }
}
