package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.model.Gender;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.DecisionService;
import com.itsmarsss.callerphone.match.service.ProfileChecklist;
import com.itsmarsss.callerphone.match.service.UpsellCopy;
import com.itsmarsss.callerphone.safety.ReportCategory;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.modals.Modal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MatchCommand implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (!ApplicationContext.isReady()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("One moment", "Still starting up. Try again shortly.")))
                    .setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String sub = e.getSubcommandName();
        if (sub == null) {
            handleHome(e, ctx, userId);
            return;
        }

        switch (sub) {
            case "join" -> handleJoin(e, ctx, userId);
            case "profile" -> handleProfile(e, ctx, userId);
            case "edit" -> handleEdit(e, ctx, userId);
            case "browse" -> {
                ctx.analytics().track(userId, "match_browse_open", null);
                handleBrowse(e, ctx, userId);
            }
            case "likes" -> handleLikes(e, ctx, userId);
            case "inbox" -> handleInbox(e, ctx, userId);
            case "chats" -> handleChats(e, ctx, userId);
            case "undo" -> {
                ctx.analytics().track(userId, "match_undo_open", null);
                handleUndo(e, ctx, userId);
            }
            case "pause" -> {
                EnrollmentService.ServiceResult r = ctx.profiles().pause(userId);
                if (r.success()) {
                    e.reply(ExperienceRenderer.toMessage(buildHome(ctx, userId, e.getUser().getName())))
                            .setEphemeral(true).queue();
                } else {
                    reply(e, r);
                }
            }
            case "resume" -> {
                EnrollmentService.ServiceResult r = ctx.profiles().resume(userId);
                if (r.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.liveReady())).setEphemeral(true).queue();
                } else {
                    reply(e, r);
                }
            }
            case "notify" -> {
                boolean enabled = e.getOption("enabled") == null || e.getOption("enabled").getAsBoolean();
                var r = ctx.enrollment().setNotifications(userId, enabled);
                var user = ctx.enrollment().getOrCreate(userId);
                e.reply(ExperienceRenderer.toMessage(r.success()
                                ? MatchPresenter.settings(user.isNotificationsEnabled(), user.isDigestOptIn())
                                : MatchPresenter.serviceFailed(r.message())
                        ))
                        .setEphemeral(true).queue();
            }
            case "digest" -> {
                boolean enabled = e.getOption("enabled") != null && e.getOption("enabled").getAsBoolean();
                var r = ctx.enrollment().setDigestOptIn(userId, enabled);
                var user = ctx.enrollment().getOrCreate(userId);
                e.reply(ExperienceRenderer.toMessage(r.success()
                                ? MatchPresenter.settings(user.isNotificationsEnabled(), user.isDigestOptIn())
                                : MatchPresenter.serviceFailed(r.message())
                        ))
                        .setEphemeral(true).queue();
            }
            case "leave" -> {
                try {
                    ctx.analytics().track(userId, "match_leave_prompt", null);
                } catch (Exception ignored) {
                }
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.leaveConfirm()))
                        .setEphemeral(true).queue();
            }
            case "delete" -> {
                try {
                    ctx.analytics().track(userId, "match_delete_prompt", null);
                } catch (Exception ignored) {
                }
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.deleteConfirm()))
                        .setEphemeral(true).queue();
            }
            case "export" -> handleExport(e, ctx, userId);
            case "premium" -> {
                ctx.analytics().track(userId, "premium_view", ctx.premium().isPremium(userId) ? "entitled" : "free");
                e.reply(ExperienceRenderer.toMessage(
                                MatchPresenter.premiumOverview(
                                        ctx.premium().isPremium(userId),
                                        ctx.premium().purchasesLive())))
                        .setEphemeral(true).queue();
            }
            case "photo" -> {
                String url = e.getOption("url") == null ? "" : e.getOption("url").getAsString().trim();
                if (url.isEmpty()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.photoMenu())).setEphemeral(true).queue();
                    return;
                }
                EnrollmentService.ServiceResult r = ctx.profiles().addPhotoUrl(userId, url);
                if (r.success()) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.photoUpdated())).setEphemeral(true).queue();
                } else {
                    reply(e, r);
                }
            }
            case "settings" -> {
                var user = ctx.enrollment().getOrCreate(userId);
                e.reply(ExperienceRenderer.toMessage(
                                MatchPresenter.settings(user.isNotificationsEnabled(), user.isDigestOptIn())))
                        .setEphemeral(true).queue();
            }
            case "safety" -> handleSafety(e, ctx, userId);
            case "submit" -> {
                ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
                EnrollmentService.ServiceResult result = ctx.profiles().publish(userId);
                if (result.success()) {
                    try {
                        ctx.analytics().track(userId, "match_go_live", "slash");
                    } catch (Exception ignored) {
                    }
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.liveReady())).setEphemeral(true).queue();
                } else {
                    var user = ctx.enrollment().getOrCreate(userId);
                    var profile = ctx.profiles().find(userId).orElse(null);
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.incompleteWelcome(
                            ProfileChecklist.format(user, profile) + "\n\n_" + result.message() + "_"
                    ))).setEphemeral(true).queue();
                }
            }
            default -> e.reply(ExperienceRenderer.toMessage(MatchPresenter.expired()))
                    .setEphemeral(true).queue();
        }
    }

    private void handleHome(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        var home = buildHome(ctx, userId, e.getUser().getName());
        try {
            String state = homeStateMeta(home);
            ctx.analytics().track(userId, "home_open", state);
            ctx.analytics().trackSurface(userId, "home", "open", state);
        } catch (Exception ignored) {
        }
        e.reply(ExperienceRenderer.toMessage(home)).setEphemeral(true).queue();
    }

    private static String homeStateMeta(com.itsmarsss.callerphone.experience.ExperienceView home) {
        if (home == null || home.title() == null) {
            return "unknown";
        }
        String t = home.title().toLowerCase();
        String d = home.description() == null ? "" : home.description().toLowerCase();
        if (t.contains("callerphone") && d.contains("optional")) {
            return "not_enrolled";
        }
        if (d.contains("finish your profile") || d.contains("next:")) {
            return "incomplete";
        }
        if (d.contains("paused")) {
            return "paused";
        }
        if (d.contains("unread chat") || d.contains("unread update")) {
            return "unread";
        }
        if (d.contains("someone is interested")) {
            return "interest";
        }
        if (d.contains("discoveries are done") || d.contains("refreshes tomorrow")) {
            return "daily_limit";
        }
        return "normal";
    }

    public static com.itsmarsss.callerphone.experience.ExperienceView buildHome(
            ApplicationContext ctx,
            String userId,
            String fallbackName
    ) {
        var user = ctx.enrollment().getOrCreate(userId);
        Optional<MatchProfile> profile = ctx.profiles().find(userId);
        boolean enrolled = user.isEnrolled();
        boolean paused = profile.isPresent()
                && profile.get().getState() == com.itsmarsss.callerphone.match.model.ProfileState.PAUSED;
        boolean live = profile.isPresent()
                && profile.get().getState() == com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE
                && ProfileChecklist.readyToSubmit(profile.get());
        String name = profile.map(MatchProfile::getDisplayName).orElse(fallbackName);
        int unread = 0;
        long left = 0;
        int incoming = 0;
        int inboxUnread = ctx.inbox().unreadCount(userId);
        if (live || paused) {
            if (profile.isPresent()) {
                MatchProfile p = profile.get();
                ctx.profiles().resetDailyCountersIfNeeded(p);
                left = Math.max(0, ctx.premium().dailyDiscoveries(userId) - p.getDiscoveryViewsToday());
            }
            for (MatchConversation chat : ctx.conversations().list(userId)) {
                unread += chat.unreadFor(userId);
            }
            incoming = ctx.incomingLikes(userId).size();
        }
        var view = MatchPresenter.home(name, unread, left, live, enrolled, paused, incoming, inboxUnread);
        // Enrich incomplete home description with checklist when available
        if (enrolled && !live && !paused && profile.isPresent()) {
            String checklist = ProfileChecklist.format(user, profile.get());
            String next = ProfileChecklist.nextStep(user, profile.get());
            return MatchPresenter.incompleteWelcome(checklist + "\n\n_Next:_ " + next);
        }
        return view;
    }

    private void handleJoin(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        EnrollmentService.ServiceResult result = ctx.enrollment().beginJoin(userId);
        if (!result.success()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.serviceFailed(result.message())))
                    .setEphemeral(true).queue();
            return;
        }
        if (!"START_ONBOARDING".equals(result.message())) {
            MatchProfile profile = ctx.profiles().getOrCreateDraft(userId);
            if (!ProfileChecklist.readyToSubmit(profile)
                    || profile.getState() != com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE) {
                var user = ctx.enrollment().getOrCreate(userId);
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.incompleteWelcome(
                                ProfileChecklist.format(user, profile)
                        )))
                        .setEphemeral(true).queue();
                return;
            }
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.liveReady()))
                    .setEphemeral(true).queue();
            return;
        }
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.joinIntro(userId)))
                .setEphemeral(true)
                .queue();
    }

    private void handleProfile(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        Optional<MatchProfile> profile = ctx.profiles().find(userId);
        if (profile.isEmpty()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.home(
                    e.getUser().getName(), 0, 0, false, false
            ))).setEphemeral(true).queue();
            return;
        }
        MatchProfile p = profile.get();
        ctx.profiles().resetDailyCountersIfNeeded(p);
        List<Button> row = new ArrayList<>();
        if (p.getState() != com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE
                || !ProfileChecklist.readyToSubmit(p)) {
            row.add(Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Finish setup"));
            row.add(Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PHOTO_MENU, "_"), "Photo"));
        } else {
            row.add(Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover"));
            row.add(Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_EDIT_MENU, "_"), "Edit"));
            row.add(Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETTINGS, "_"), "Settings"));
        }
        e.replyEmbeds(MatchEmbeds.profileCard(p, true))
                .addComponents(ActionRow.of(row))
                .setEphemeral(true)
                .queue();
        try {
            ctx.analytics().track(userId, "match_profile_view", p.getState() == null ? "" : p.getState().name());
        } catch (Exception ignored) {
        }
    }

    private void handleEdit(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        try {
            ctx.analytics().track(userId, "edit_menu_open", "slash");
        } catch (Exception ignored) {
        }
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.editMenu())).setEphemeral(true).queue();
    }

    private void handleBrowse(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        try {
            ctx.analytics().track(userId, "discover_open", "slash");
        } catch (Exception ignored) {
        }
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> DiscoveryUi.sendDiscover(e.getHook(), ctx, userId, true));
    }

    private void handleLikes(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        List<com.itsmarsss.callerphone.match.model.MatchDecision> incoming = ctx.incomingLikes(userId);
        if (incoming.isEmpty()) {
            try {
                ctx.analytics().track(userId, "likes_open", "empty");
            } catch (Exception ignored) {
            }
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.incomingInterestEmpty()))
                    .setEphemeral(true).queue();
            return;
        }
        // Plan §7: free path is a teaser; Premium reveals names
        if (!ctx.premium().canSeeIncomingInterestNames(userId)) {
            try {
                ctx.analytics().track(userId, "likes_open", "teaser");
            } catch (Exception ignored) {
            }
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.incomingInterestFreeTeaser()))
                    .setEphemeral(true).queue();
            return;
        }
        try {
            ctx.analytics().track(userId, "likes_open", "list");
        } catch (Exception ignored) {
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
        sb.append("\nDiscover to express interest back.");
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.incomingInterestList(sb.toString())))
                .setEphemeral(true).queue();
    }

    private void handleInbox(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        try {
            ctx.analytics().track(userId, "inbox_open", "slash");
        } catch (Exception ignored) {
        }
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> InboxUi.send(e.getHook(), ctx, userId));
    }

    private void handleChats(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        String action = e.getOption("action") == null ? "list" : e.getOption("action").getAsString();
        if ("stop".equals(action)) {
            EnrollmentService.ServiceResult r = ctx.conversations().stopChat(userId);
            if (r.success()) {
                try {
                    ctx.analytics().track(userId, "chat_stop", "slash");
                } catch (Exception ignored) {
                }
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.serviceDone(
                        r.message() + "\n\nOpen chats anytime — or Discover someone new."
                ))).setEphemeral(true).queue();
            } else {
                reply(e, r);
            }
            return;
        }
        try {
            ctx.analytics().track(userId, "chats_open", "slash");
        } catch (Exception ignored) {
        }
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> ChatInboxUi.sendInbox(e.getHook(), ctx, userId));
    }

    private void handleUndo(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> {
            DecisionService.DecisionResult result = ctx.decisions().undoLastSkip(userId);
            if (!result.success() || result.restoredSession() == null) {
                String msg = result.message() == null ? "" : result.message();
                if (msg.toLowerCase().contains("nothing left") || msg.toLowerCase().contains("too late")) {
                    try {
                        ctx.analytics().track(userId, "soft_limit", "undo");
                        ctx.analytics().trackSurface(userId, "discover", "soft_limit", "undo");
                    } catch (Exception ignored) {
                    }
                    e.getHook().sendMessage(ExperienceRenderer.toMessage(
                            MatchPresenter.softLimit("Undo unavailable", msg)
                    )).setEphemeral(true).queue();
                } else {
                    e.getHook().sendMessage(ExperienceRenderer.toMessage(
                            MatchPresenter.serviceFailed(result.message())
                    )).setEphemeral(true).queue();
                }
                return;
            }
            Optional<MatchProfile> profile = ctx.profiles().find(result.restoredSession().subjectId());
            if (profile.isEmpty()) {
                e.getHook().sendMessage(ExperienceRenderer.toMessage(
                        MatchPresenter.serviceFailed(result.message())
                )).setEphemeral(true).queue();
                return;
            }
            try {
                ctx.analytics().track(userId, "discover_undo", "ok");
                ctx.analytics().trackSurface(userId, "discover", "undo", "ok");
            } catch (Exception ignored) {
            }
            String remaining = DiscoveryUi.remainingLine(ctx, userId);
            MatchProfile viewer = ctx.profiles().find(userId).orElse(null);
            e.getHook().sendMessage(DiscoveryUi.cardMessage(
                            profile.get(),
                            viewer,
                            result.restoredSession(),
                            remaining,
                            result.message()
                    ))
                    .setEphemeral(true)
                    .queue();
        });
    }

    private void handleExport(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> {
            try {
                String json = ctx.export().exportJson(userId);
                byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                ctx.analytics().track(userId, "match_export", String.valueOf(bytes.length));
                e.getHook().sendMessage(ExperienceRenderer.toMessage(MatchPresenter.serviceDone(
                                "Your export is ready. It includes your profile, settings, and conversations covered by the export policy."
                        )))
                        .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(bytes, "callerphone-match-export.json"))
                        .setEphemeral(true)
                        .queue();
            } catch (Exception ex) {
                e.getHook().sendMessage(ExperienceRenderer.toMessage(MatchPresenter.serviceFailed(
                        "Export failed. Try again in a moment. Nothing was deleted."
                ))).setEphemeral(true).queue();
            }
        });
    }

    private void handleSafety(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        String action = e.getOption("action") == null ? "help" : e.getOption("action").getAsString();
        String target = e.getOption("user_id") == null ? null : e.getOption("user_id").getAsString();
        String reason = e.getOption("reason") == null ? "" : e.getOption("reason").getAsString();
        String category = e.getOption("category") == null ? "other" : e.getOption("category").getAsString();
        String conversationId = e.getOption("conversation_id") == null
                ? null
                : e.getOption("conversation_id").getAsString();
        boolean staff = Users.isModerator(userId);
        switch (action) {
            case "block" -> {
                if (target == null) {
                    e.reply(ExperienceRenderer.toMessage(staff
                            ? MatchPresenter.warn("Block", "Add a user id (staff only).")
                            : MatchPresenter.safetyHelp()
                    )).setEphemeral(true).queue();
                    return;
                }
                if (!staff) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
                    return;
                }
                ctx.safety().block(userId, target, reason);
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyBlocked(null))).setEphemeral(true).queue();
            }
            case "report" -> {
                if (target == null) {
                    e.reply(ExperienceRenderer.toMessage(staff
                            ? MatchPresenter.warn("Report", "Add a user id (staff only).")
                            : MatchPresenter.safetyHelp()
                    )).setEphemeral(true).queue();
                    return;
                }
                if (!staff) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
                    return;
                }
                ReportCategory cat = ReportCategory.from(category).orElse(ReportCategory.OTHER);
                ctx.safety().report(
                        userId,
                        target,
                        cat.code(),
                        reason.isBlank() ? cat.label() : reason,
                        conversationId != null ? "conversation" : "user",
                        conversationId != null ? conversationId : target
                );
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyReported())).setEphemeral(true).queue();
            }
            case "unmatch" -> {
                if (conversationId == null && target == null) {
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
                    return;
                }
                reply(e, ctx.conversations().unmatch(userId, conversationId != null ? conversationId : target));
            }
            default -> e.reply(ExperienceRenderer.toMessage(MatchPresenter.safetyHelp())).setEphemeral(true).queue();
        }
    }

    private static void reply(SlashCommandInteractionEvent e, EnrollmentService.ServiceResult result) {
        e.reply(ExperienceRenderer.toMessage(result.success()
                ? MatchPresenter.serviceDone(result.message())
                : MatchPresenter.serviceFailed(result.message())
        )).setEphemeral(true).queue();
    }

    /**
     * One form for the whole profile (Discord max 5 inputs).
     * Guided join opens this right after age pick — no extra slash commands.
     */
    static Modal setupModal() {
        return Modal.create("m-v1-modal-setup", "Your profile")
                .addComponents(
                        Label.of("Display name", TextInput.create("displayName", TextInputStyle.SHORT)
                                .setPlaceholder("How you show up")
                                .setRequired(true).setMaxLength(32).build()),
                        Label.of("Bio", TextInput.create("bio", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("A little about you")
                                .setRequired(true).setMaxLength(300).build()),
                        Label.of("Ideal Sunday", TextInput.create("prompt", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("What does yours look like?")
                                .setRequired(true).setMaxLength(200).build()),
                        Label.of("Interests", TextInput.create("interests", TextInputStyle.SHORT)
                                .setPlaceholder("gaming, music, art…")
                                .setRequired(true).setMaxLength(80).build()),
                        Label.of("Pronouns", TextInput.create("pronouns", TextInputStyle.SHORT)
                                .setPlaceholder("optional")
                                .setRequired(false).setMaxLength(24).build())
                )
                .build();
    }

    /** @deprecated use {@link #setupModal()} */
    @Deprecated
    static Modal basicsModal() {
        return setupModal();
    }

    /** @deprecated use {@link #setupModal()} */
    @Deprecated
    static Modal bioModal() {
        return setupModal();
    }

    /** @deprecated use {@link #setupModal()} */
    @Deprecated
    static Modal interestsModal() {
        return setupModal();
    }

    static List<Gender> parseOpenTo(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(Gender::fromCode)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    @Override
    public String getHelp() {
        return "`/match join` get started\n"
                + "`/match browse` discover people\n"
                + "`/match chats` · `/match likes` · `/match inbox`\n"
                + "`/match settings` · `/match premium` · `/match photo`\n"
                + "`/match safety` block, report, unmatch";
    }

    @Override
    public String getName() {
        return "match";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Meet people and chat safely")
                .addSubcommands(
                        new SubcommandData("join", "Get started"),
                        new SubcommandData("profile", "Your card"),
                        new SubcommandData("edit", "Edit profile"),
                        new SubcommandData("browse", "Discover people"),
                        new SubcommandData("likes", "Incoming interest"),
                        new SubcommandData("inbox", "Unified social inbox"),
                        new SubcommandData("undo", "Undo your last skip"),
                        new SubcommandData("chats", "List or select mediated chats")
                                .addOptions(new OptionData(OptionType.STRING, "action", "list or stop", false)
                                        .addChoice("list", "list")
                                        .addChoice("stop", "stop")),
                        new SubcommandData("pause", "Pause your profile"),
                        new SubcommandData("resume", "Resume a paused profile"),
                        new SubcommandData("notify", "Toggle Match DMs")
                                .addOptions(new OptionData(OptionType.BOOLEAN, "enabled", "Receive Match DMs", true)),
                        new SubcommandData("digest", "Weekly opt-in digest")
                                .addOptions(new OptionData(OptionType.BOOLEAN, "enabled", "Enable weekly digest", true)),
                        new SubcommandData("leave", "Leave discovery (keeps profile & chats)"),
                        new SubcommandData("delete", "Hard-wipe Match profile content"),
                        new SubcommandData("export", "Export your Match data as JSON"),
                        new SubcommandData("premium", "Premium overview"),
                        new SubcommandData("photo", "Profile image (avatar or https URL)")
                                .addOptions(new OptionData(OptionType.STRING, "url", "https image URL", false)),
                        new SubcommandData("settings", "Notification settings"),
                        new SubcommandData("submit", "Go live in discovery"),
                        new SubcommandData("safety", "Block, report, or unmatch")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "action", "block/report/unmatch/help", true)
                                                .addChoice("help", "help")
                                                .addChoice("block", "block")
                                                .addChoice("report", "report")
                                                .addChoice("unmatch", "unmatch"),
                                        new OptionData(OptionType.STRING, "user_id", "Target user id", false),
                                        new OptionData(OptionType.STRING, "conversation_id", "Conversation id for evidence", false),
                                        new OptionData(OptionType.STRING, "category", "Report category", false)
                                                .addChoice("harassment", "harassment")
                                                .addChoice("spam", "spam")
                                                .addChoice("inappropriate", "inappropriate")
                                                .addChoice("age_lie", "age_lie")
                                                .addChoice("grooming", "grooming")
                                                .addChoice("threats", "threats")
                                                .addChoice("contact", "contact")
                                                .addChoice("impersonation", "impersonation")
                                                .addChoice("other", "other"),
                                        new OptionData(OptionType.STRING, "reason", "Details", false)
                                )
                )
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL);
    }
}
