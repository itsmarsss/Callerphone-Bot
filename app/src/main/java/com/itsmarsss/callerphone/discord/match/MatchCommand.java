package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.model.Gender;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.DecisionService;
import com.itsmarsss.callerphone.match.service.DiscoveryService;
import com.itsmarsss.callerphone.match.service.EmptyStates;
import com.itsmarsss.callerphone.match.service.ProfileChecklist;
import com.itsmarsss.callerphone.match.service.UpsellCopy;
import com.itsmarsss.callerphone.safety.ReportCategory;
import com.itsmarsss.commandType.ISlashCommand;
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
            e.replyEmbeds(MatchEmbeds.warm("One moment", "Still starting up. Try again shortly."))
                    .setEphemeral(true).queue();
            return;
        }
        String sub = e.getSubcommandName();
        if (sub == null) {
            e.replyEmbeds(MatchEmbeds.soft("Discover", "Try `/match join` or `/match browse`.")).setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();

        switch (sub) {
            case "join" -> handleJoin(e, ctx, userId);
            case "profile" -> handleProfile(e, ctx, userId);
            case "edit" -> handleEdit(e, ctx, userId);
            case "browse" -> {
                ctx.analytics().track(userId, "match_browse_open", null);
                handleBrowse(e, ctx, userId);
            }
            case "likes" -> handleLikes(e, ctx, userId);
            case "chats" -> handleChats(e, ctx, userId);
            case "undo" -> handleUndo(e, ctx, userId);
            case "pause" -> reply(e, ctx.profiles().pause(userId));
            case "resume" -> reply(e, ctx.profiles().resume(userId));
            case "notify" -> {
                boolean enabled = e.getOption("enabled") == null || e.getOption("enabled").getAsBoolean();
                reply(e, ctx.enrollment().setNotifications(userId, enabled));
            }
            case "digest" -> {
                boolean enabled = e.getOption("enabled") != null && e.getOption("enabled").getAsBoolean();
                reply(e, ctx.enrollment().setDigestOptIn(userId, enabled));
            }
            case "leave" -> reply(e, ctx.deletion().leaveAndSoftDelete(userId));
            case "delete" -> reply(e, ctx.deletion().hardDeleteProfileContent(userId));
            case "export" -> handleExport(e, ctx, userId);
            case "premium" -> e.replyEmbeds(MatchEmbeds.simple("Premium", UpsellCopy.premiumPitch()))
                    .setEphemeral(true).queue();
            case "photo" -> {
                String url = e.getOption("url") == null ? "" : e.getOption("url").getAsString();
                reply(e, ctx.profiles().addPhotoUrl(userId, url));
            }
            case "safety" -> handleSafety(e, ctx, userId);
            case "submit" -> {
                ctx.profiles().setAvatar(userId, e.getUser().getEffectiveAvatarUrl());
                reply(e, ctx.profiles().publish(userId));
            }
            default -> e.replyEmbeds(MatchEmbeds.warm("Unknown", "That command isn't recognized.")).setEphemeral(true).queue();
        }
    }

    private void handleJoin(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        EnrollmentService.ServiceResult result = ctx.enrollment().beginJoin(userId);
        if (!result.success()) {
            e.replyEmbeds(MatchEmbeds.warm("Can't continue", result.message())).setEphemeral(true).queue();
            return;
        }
        if (!"START_ONBOARDING".equals(result.message())) {
            MatchProfile profile = ctx.profiles().getOrCreateDraft(userId);
            if (!ProfileChecklist.readyToSubmit(profile)
                    || profile.getState() != com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE) {
                e.reply(ExperienceRenderer.toMessage(MatchPresenter.incompleteWelcome()))
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
            e.replyEmbeds(MatchEmbeds.warm("No profile yet", "Start with `/match join`.")).setEphemeral(true).queue();
            return;
        }
        MatchProfile p = profile.get();
        ctx.profiles().resetDailyCountersIfNeeded(p);
        List<Button> row = new ArrayList<>();
        if (p.getState() != com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE
                || !ProfileChecklist.readyToSubmit(p)) {
            row.add(Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Finish setup"));
        } else {
            row.add(Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover"));
            row.add(Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit"));
        }
        e.replyEmbeds(MatchEmbeds.profileCard(p, true))
                .addComponents(ActionRow.of(row))
                .setEphemeral(true)
                .queue();
    }

    private void handleEdit(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        // Single setup form — field option kept for compat but ignored for lower friction
        e.replyModal(setupModal()).queue();
    }

    private void handleBrowse(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> {
            DiscoveryService.DiscoveryResult result = ctx.discovery().next(userId);
            if (!result.success()) {
                e.getHook().sendMessageEmbeds(MatchEmbeds.soft("Discover", result.message())).setEphemeral(true).queue();
                return;
            }
            String sessionId = result.session().sessionId();
            Optional<MatchProfile> self = ctx.profiles().find(userId);
            String remaining = self.map(p -> {
                ctx.profiles().resetDailyCountersIfNeeded(p);
                long left = Math.max(0, ctx.premium().dailyDiscoveries(userId) - p.getDiscoveryViewsToday());
                return left + " left today";
            }).orElse("");
            e.getHook().sendMessageEmbeds(MatchEmbeds.profileCard(result.profile(), false))
                    .setContent(remaining.isBlank() ? null : remaining)
                    .addComponents(ActionRow.of(
                            Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, sessionId), "Interested"),
                            Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, sessionId), "Next")
                    ))
                    .setEphemeral(true)
                    .queue();
        });
    }

    private void handleLikes(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        // Free for everyone — uses decision repository via discovery path
        var decisions = ctx.decisions();
        // pull via reflection-free: use profile service + a thin path on Discovery/Decision
        List<com.itsmarsss.callerphone.match.model.MatchDecision> incoming =
                findIncoming(ctx, userId);
        if (incoming.isEmpty()) {
            e.replyEmbeds(MatchEmbeds.soft("Incoming interest", EmptyStates.noLikes())).setEphemeral(true).queue();
            return;
        }
        StringBuilder sb = new StringBuilder("People who expressed interest in you:\n");
        int i = 1;
        for (var d : incoming) {
            String name = ctx.profiles().find(d.viewerId()).map(MatchProfile::getDisplayName).orElse(d.viewerId());
            sb.append(i++).append(". **").append(name).append("**\n");
            if (i > 15) {
                break;
            }
        }
        e.replyEmbeds(MatchEmbeds.soft("Incoming interest", sb + "\nDiscover to express interest back.")).setEphemeral(true).queue();
    }

    private static List<com.itsmarsss.callerphone.match.model.MatchDecision> findIncoming(ApplicationContext ctx, String userId) {
        // Access via Mongo repo is not exposed; use Safety/Decision through a package helper on ApplicationContext
        return ctx.incomingLikes(userId);
    }

    private void handleChats(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        String action = e.getOption("action") == null ? "list" : e.getOption("action").getAsString();
        if ("stop".equals(action)) {
            reply(e, ctx.conversations().stopChat(userId));
            return;
        }
        List<MatchConversation> chats = ctx.conversations().list(userId);
        if (chats.isEmpty()) {
            e.replyEmbeds(MatchEmbeds.soft("Chats", EmptyStates.noChats())).setEphemeral(true).queue();
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<ActionRow> rows = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        for (MatchConversation chat : chats) {
            String other = chat.otherParticipant(userId);
            String name = ctx.profiles().find(other).map(MatchProfile::getDisplayName).orElse("Match");
            int unread = chat.unreadFor(userId);
            String badge = unread > 0 ? " · " + unread + " new" : "";
            String preview = chat.getLastMessagePreview() == null || chat.getLastMessagePreview().isBlank()
                    ? ""
                    : "\n_" + truncate(chat.getLastMessagePreview(), 50) + "_";
            sb.append("**").append(name).append("**").append(badge).append(preview).append("\n\n");
            buttons.add(Button.primary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, chat.getConversationId()),
                    truncate(name, 20)
            ));
            if (buttons.size() == 5) {
                rows.add(ActionRow.of(buttons));
                buttons = new ArrayList<>();
            }
        }
        if (!buttons.isEmpty()) {
            rows.add(ActionRow.of(buttons));
        }
        e.replyEmbeds(MatchEmbeds.soft("Chats", sb.toString().trim()))
                .setComponents(rows)
                .setEphemeral(true)
                .queue();
    }

    private void handleUndo(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> {
            DecisionService.DecisionResult result = ctx.decisions().undoLastSkip(userId);
            if (!result.success() || result.restoredSession() == null) {
                e.getHook().sendMessageEmbeds(MatchEmbeds.soft("Discover", result.message())).setEphemeral(true).queue();
                return;
            }
            Optional<MatchProfile> profile = ctx.profiles().find(result.restoredSession().subjectId());
            if (profile.isEmpty()) {
                e.getHook().sendMessageEmbeds(MatchEmbeds.soft("Discover", result.message())).setEphemeral(true).queue();
                return;
            }
            String sessionId = result.restoredSession().sessionId();
            e.getHook().sendMessageEmbeds(MatchEmbeds.profileCard(profile.get(), false))
                    .setContent(result.message())
                    .addComponents(ActionRow.of(
                            Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, sessionId), "Interested"),
                            Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, sessionId), "Next")
                    ))
                    .setEphemeral(true)
                    .queue();
        });
    }

    private void handleExport(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        String json = ctx.export().exportJson(userId);
        if (json.length() > 1800) {
            json = json.substring(0, 1800) + "\n… truncated";
        }
        e.reply("```json\n" + json + "\n```").setEphemeral(true).queue();
    }

    private void handleSafety(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        String action = e.getOption("action") == null ? "help" : e.getOption("action").getAsString();
        String target = e.getOption("user_id") == null ? null : e.getOption("user_id").getAsString();
        String reason = e.getOption("reason") == null ? "" : e.getOption("reason").getAsString();
        String category = e.getOption("category") == null ? "other" : e.getOption("category").getAsString();
        String conversationId = e.getOption("conversation_id") == null
                ? null
                : e.getOption("conversation_id").getAsString();
        switch (action) {
            case "block" -> {
                if (target == null) {
                    e.reply("Provide user_id to block.").setEphemeral(true).queue();
                    return;
                }
                ctx.safety().block(userId, target, reason);
                e.replyEmbeds(MatchEmbeds.success("Blocked", "They won't show up for you here.")).setEphemeral(true).queue();
            }
            case "report" -> {
                if (target == null) {
                    e.reply("Provide user_id to report.").setEphemeral(true).queue();
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
                e.replyEmbeds(MatchEmbeds.success("Report received", "Thanks. They haven't been notified.")).setEphemeral(true).queue();
            }
            case "unmatch" -> {
                if (target == null && conversationId == null) {
                    e.reply("Need a conversation id.").setEphemeral(true).queue();
                    return;
                }
                reply(e, ctx.conversations().unmatch(userId, conversationId != null ? conversationId : target));
            }
            default -> e.replyEmbeds(MatchEmbeds.soft(
                    "Safety",
                    "Block, report, or unmatch from here.\nPick an action and fill the fields Discord shows."
            )).setEphemeral(true).queue();
        }
    }

    private static void reply(SlashCommandInteractionEvent e, EnrollmentService.ServiceResult result) {
        e.replyEmbeds(result.success() ? MatchEmbeds.success("Done", result.message()) : MatchEmbeds.warm("Couldn't complete", result.message())).setEphemeral(true).queue();
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
                + "`/match profile` · `/match likes` · `/match chats`\n"
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
                        new SubcommandData("premium", "Premium (not available yet)"),
                        new SubcommandData("photo", "Add optional profile photo URL")
                                .addOptions(new OptionData(OptionType.STRING, "url", "https image URL", true)),
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
