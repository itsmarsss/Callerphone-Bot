package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
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
                try {
                    ctx.analytics().track(userId, "discover_open", "button");
                } catch (Exception ignored) {
                }
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> DiscoveryUi.sendDiscover(e.getHook(), ctx, userId, true));
            }
            case MatchComponentIds.ACTION_OPEN_CHATS -> {
                try {
                    ctx.analytics().track(userId, "chats_open", "button");
                } catch (Exception ignored) {
                }
                e.deferReply(true).queue();
                ctx.dbExecutor().execute(() -> ChatInboxUi.sendInbox(e.getHook(), ctx, userId));
            }
            case MatchComponentIds.ACTION_INTERESTED -> decide(e, ctx, userId, opaque, DecisionType.INTERESTED);
            case MatchComponentIds.ACTION_SKIP -> decide(e, ctx, userId, opaque, DecisionType.SKIP);
            case MatchComponentIds.ACTION_CHAT_SELECT -> replyChatSelect(e, ctx, userId, opaque);
            case MatchComponentIds.ACTION_CONNECT_REQUEST -> {
                EnrollmentService.ServiceResult result = ctx.connect().request(userId, opaque);
                if (result.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.quietSuccess(
                            "Request sent",
                            result.message() + "\n\nKeep chatting while you wait — they have 48 hours."
                    ))).setEphemeral(true).queue();
                } else {
                    replyService(e, result);
                }
            }
            case MatchComponentIds.ACTION_CONNECT_ACCEPT -> {
                var result = ctx.connect().accept(userId, opaque);
                if (result.success()) {
                    String peerName = result.otherUserId() == null
                            ? "your connection"
                            : ctx.profiles().find(result.otherUserId())
                                    .map(MatchProfile::getDisplayName)
                                    .orElse("your connection");
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.connected(
                            peerName,
                            null,
                            opaque
                    ))).setEphemeral(true).queue();
                } else {
                    e.reply(ExperienceRenderer.toMessage(
                            MatchPresenter.warn("Couldn't complete", result.message())
                    )).setEphemeral(true).queue();
                }
            }
            case MatchComponentIds.ACTION_CONNECT_DECLINE -> {
                var result = ctx.connect().decline(userId, opaque);
                if (result.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.quietSuccess(
                            "Declined",
                            result.message() + "\n\nYou can keep chatting or open another connection."
                    ))).setEphemeral(true).queue();
                } else {
                    replyService(e, result);
                }
            }
            case MatchComponentIds.ACTION_UNMATCH -> replyService(e, ctx.conversations().unmatch(userId, opaque));
            case MatchComponentIds.ACTION_STOP_CHAT -> replyService(e, ctx.conversations().stopChat(userId));
            case MatchComponentIds.ACTION_SUBMIT -> {
                ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
                EnrollmentService.ServiceResult result = ctx.profiles().publish(userId);
                if (result.success()) {
                    try {
                        ctx.analytics().track(userId, "match_go_live", "button");
                    } catch (Exception ignored) {
                    }
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.liveReady())).setEphemeral(true).queue();
                } else {
                    var user = ctx.enrollment().getOrCreate(userId);
                    var profile = ctx.profiles().find(userId).orElse(null);
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.incompleteWelcome(
                            com.itsmarsss.callerphone.match.service.ProfileChecklist.format(user, profile)
                                    + "\n\n_" + result.message() + "_"
                    ))).setEphemeral(true).queue();
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
            case MatchComponentIds.ACTION_LEAVE_CONFIRM -> {
                var result = ctx.deletion().leaveAndSoftDelete(userId);
                if (result.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.home(
                            e.getUser().getName(), 0, 0, false, false
                    ))).setEphemeral(true).queue();
                } else {
                    replyService(e, result);
                }
            }
            case MatchComponentIds.ACTION_LEAVE_CANCEL -> e.reply(ExperienceRenderer.toMessage(
                            MatchPresenter.quietSuccess("Still live", "You're still in Discover.")
                    )).setEphemeral(true).queue();
            case MatchComponentIds.ACTION_DELETE_CONFIRM -> {
                var result = ctx.deletion().hardDeleteProfileContent(userId);
                if (result.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.quietSuccess(
                            "Deleted",
                            result.message() + "\n\nYou can start fresh with **Create profile** anytime."
                    ))).setEphemeral(true).queue();
                } else {
                    replyService(e, result);
                }
            }
            case MatchComponentIds.ACTION_DELETE_CANCEL -> e.reply(ExperienceRenderer.toMessage(
                            MatchPresenter.quietSuccess("Cancelled", "Nothing was deleted.")
                    )).setEphemeral(true).queue();
            case MatchComponentIds.ACTION_PREVIEW_SELF -> {
                Optional<MatchProfile> p = ctx.profiles().find(userId);
                if (p.isEmpty()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("No profile", "Start with `/match join`.")))
                            .setEphemeral(true).queue();
                } else {
                    e.replyEmbeds(MatchEmbeds.profileCard(p.get(), true))
                            .addComponents(ActionRow.of(
                                    Button.primary(
                                            MatchComponentIds.of(MatchComponentIds.ACTION_EDIT_MENU, "_"),
                                            "Edit"
                                    ),
                                    Button.success(
                                            MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                                            "Discover"
                                    ),
                                    Button.secondary(
                                            MatchComponentIds.of(MatchComponentIds.ACTION_SETTINGS, "_"),
                                            "Settings"
                                    )
                            ))
                            .setEphemeral(true).queue();
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
                    try {
                        ctx.analytics().track(userId, "match_photo_avatar", null);
                    } catch (Exception ignored) {
                    }
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.photoUpdated())).setEphemeral(true).queue();
                } else {
                    replyService(e, r);
                }
            }
            case MatchComponentIds.ACTION_PREMIUM -> e.reply(ExperienceRenderer.toMessage(
                    MatchPresenter.premiumOverview(
                            ctx.premium().isPremium(userId),
                            ctx.premium().purchasesLive()
                    )
            )).setEphemeral(true).queue();
            case MatchComponentIds.ACTION_REWARDS -> {
                long credits = com.itsmarsss.database.categories.Users.getCredits(userId);
                String prefix = com.itsmarsss.database.categories.Users.getPrefix(userId);
                long total = com.itsmarsss.database.categories.Users.getExecuted(userId)
                        + com.itsmarsss.database.categories.Users.getTransmitted(userId);
                int level = (int) (total / 100);
                try {
                    ctx.analytics().track(userId, "rewards_view", null);
                } catch (Exception ignored) {
                }
                e.reply(ExperienceRenderer.toMessage(
                        MatchPresenter.rewardsCatalog(level, credits, prefix)
                )).setEphemeral(true).queue();
            }
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
                try {
                    ctx.analytics().track(userId, "inbox_open",
                            MatchComponentIds.ACTION_HOME.equals(action) ? "home_btn" : "back_inbox");
                } catch (Exception ignored) {
                }
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
            case MatchComponentIds.ACTION_ICEBREAKER -> {
                var conv = ctx.conversations().find(opaque);
                if (conv.isEmpty() || !conv.get().getParticipants().contains(userId)) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("Chat", "Chat not found.")))
                            .setEphemeral(true).queue();
                    return;
                }
                String other = conv.get().otherParticipant(userId);
                String opener = com.itsmarsss.callerphone.match.service.Icebreakers.forPair(
                        ctx.profiles().find(userId).orElse(null),
                        ctx.profiles().find(other).orElse(null)
                );
                try {
                    ctx.analytics().track(userId, "match_icebreaker", opaque);
                } catch (Exception ignored) {
                }
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.icebreakerPrompt(opener, opaque)))
                        .setEphemeral(true).queue();
            }
            case MatchComponentIds.ACTION_GAME_TTT -> {
                var result = ctx.connectionGames().proposeTtt(opaque, userId);
                e.reply(ExperienceRenderer.toMessage(result.success()
                        ? MatchPresenter.gameChallengeSent(opaque, result.message())
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
                                    ? MatchPresenter.gameStarted(opaque, result.message())
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
                        ? MatchPresenter.chatSelected(
                                "your connection",
                                result.message() + "\n\nYou can keep chatting.",
                                opaque
                        )
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
        if (result.success()) {
            e.reply(ExperienceRenderer.toMessage(
                    MatchPresenter.serviceDone(result.message())
            )).setEphemeral(true).queue();
            return;
        }
        e.reply(ExperienceRenderer.toMessage(
                MatchPresenter.serviceFailed(result.message())
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
        Optional<String> pendingGame = ctx.connectionGames().pendingProposer(conversationId);
        MatchPresenter.ChatGameUi gameUi = MatchPresenter.ChatGameUi.AVAILABLE;
        if (pendingGame.isPresent() && !pendingGame.get().equals(userId)) {
            gameUi = MatchPresenter.ChatGameUi.ACCEPT_THEIRS;
        } else if (pendingGame.isPresent()) {
            gameUi = MatchPresenter.ChatGameUi.WAITING_SELF;
        }
        boolean connectRequester = conversation.getConnectRequestedBy() != null
                && conversation.getConnectRequestedBy().equals(userId);
        boolean connectTarget = conversation.getConnectRequestedBy() != null
                && !conversation.getConnectRequestedBy().equals(userId);
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.chatSelected(
                name,
                result.message(),
                conversationId,
                conversation.getStage(),
                connectRequester,
                connectTarget,
                gameUi,
                false
        ))).setEphemeral(true).queue();
    }

    private void decide(ButtonInteraction e, ApplicationContext ctx, String userId, String sessionId, DecisionType type) {
        e.deferEdit().queue();
        ctx.dbExecutor().execute(() -> {
            DecisionService.DecisionResult result = ctx.decisions().decide(userId, sessionId, type);
            if (!result.success()) {
                String msg = result.message() == null ? "" : result.message();
                String lower = msg.toLowerCase();
                if (lower.contains("interests used") || lower.contains("chat limit") || lower.contains("limit reached")) {
                    try {
                        ctx.analytics().track(userId, "soft_limit", type.name().toLowerCase());
                    } catch (Exception ignored) {
                    }
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
        try {
            ctx.analytics().track(userId, "safety_open",
                    safety.kind() == null ? "unknown" : safety.kind().name().toLowerCase());
        } catch (Exception ignored) {
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
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyReportPrompt()))
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
