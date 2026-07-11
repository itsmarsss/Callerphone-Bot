package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
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
            e.reply(ToolSet.CP_EMJ + " Match is still starting up. Try again in a moment.")
                    .setEphemeral(true).queue();
            return;
        }
        String sub = e.getSubcommandName();
        if (sub == null) {
            e.reply(ToolSet.CP_EMJ + " Use a Match subcommand.").setEphemeral(true).queue();
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
            default -> e.reply(ToolSet.CP_EMJ + " Unknown Match subcommand.").setEphemeral(true).queue();
        }
    }

    private void handleJoin(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        EnrollmentService.ServiceResult result = ctx.enrollment().beginJoin(userId);
        if (!result.success()) {
            e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
            return;
        }
        if (!"START_ONBOARDING".equals(result.message())) {
            MatchUser user = ctx.enrollment().getOrCreate(userId);
            MatchProfile profile = ctx.profiles().getOrCreateDraft(userId);
            // Incomplete profile: one button continues setup — no command soup
            if (!ProfileChecklist.readyToSubmit(profile)
                    || profile.getState() != com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE) {
                e.replyEmbeds(MatchEmbeds.soft(
                        "Pick up where you left off",
                        "You're already in. Finish your card in **one short form** — no extra commands."
                )).addComponents(ActionRow.of(
                        Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Continue setup"),
                        Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Browse")
                )).setEphemeral(true).queue();
                return;
            }
            e.replyEmbeds(MatchEmbeds.success(
                    "You're all set",
                    result.message() + "\n\nJump into discovery anytime."
            )).addComponents(ActionRow.of(
                    Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "✦ Browse")
            )).setEphemeral(true).queue();
            return;
        }
        e.replyEmbeds(MatchEmbeds.soft(
                        "✦  Callerphone Social",
                        """
                                Meet people who vibe with you — **friendship & community**.

                                **About 30 seconds**
                                1. Agree → pick age group  
                                2. One form (name, bio, interests)  
                                3. You're live — start browsing

                                No approval queue. Age groups never mix.
                                """
                ))
                .addComponents(ActionRow.of(Button.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_JOIN_ACCEPT, userId),
                        "Sounds good — let's go"
                )))
                .setEphemeral(true)
                .queue();
    }

    private void handleProfile(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        Optional<MatchProfile> profile = ctx.profiles().find(userId);
        if (profile.isEmpty()) {
            e.reply(ToolSet.CP_EMJ + " No profile yet. Use `/match join`.").setEphemeral(true).queue();
            return;
        }
        MatchUser user = ctx.enrollment().getOrCreate(userId);
        MatchProfile p = profile.get();
        ctx.profiles().resetDailyCountersIfNeeded(p);
        MatchUser mu = user;
        String limits = "Discoveries: " + p.getDiscoveryViewsToday() + "/" + ctx.premium().dailyDiscoveries(userId)
                + " · Interests: " + p.getInterestSignalsToday() + "/" + ctx.premium().dailyInterests(userId)
                + " · Streak: " + mu.getBrowseStreakDays() + "d"
                + " · Complete: " + ProfileChecklist.completionPercent(mu, p) + "%";
        List<Button> row = new ArrayList<>();
        if (p.getState() != com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE
                || !ProfileChecklist.readyToSubmit(p)) {
            row.add(Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Finish setup"));
        } else {
            row.add(Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit profile"));
            row.add(Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Browse"));
        }
        e.replyEmbeds(
                        MatchEmbeds.profileCard(p, true),
                        MatchEmbeds.checklist(
                                "Progress · " + ProfileChecklist.completionPercent(user, p) + "%",
                                limits,
                                ProfileChecklist.format(user, p),
                                ProfileChecklist.nextStep(user, p)
                        )
                )
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
                e.getHook().sendMessage(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
                return;
            }
            String sessionId = result.session().sessionId();
            Optional<MatchProfile> self = ctx.profiles().find(userId);
            String remaining = self.map(p -> {
                ctx.profiles().resetDailyCountersIfNeeded(p);
                long left = Math.max(0, ctx.premium().dailyDiscoveries(userId) - p.getDiscoveryViewsToday());
                return left + " discoveries left today";
            }).orElse("");
            e.getHook().sendMessageEmbeds(MatchEmbeds.profileCard(result.profile(), false))
                    .setContent(ToolSet.CP_EMJ + " " + remaining)
                    .addComponents(ActionRow.of(
                            Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, sessionId), "Interested"),
                            Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, sessionId), "Skip")
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
            e.reply(ToolSet.CP_EMJ + " " + EmptyStates.noLikes()).setEphemeral(true).queue();
            return;
        }
        StringBuilder sb = new StringBuilder("People who expressed interest in you:\n");
        int i = 1;
        for (var d : incoming) {
            String name = ctx.profiles().find(d.viewerId()).map(MatchProfile::getDisplayName).orElse(d.viewerId());
            sb.append(i++).append(". **").append(name).append("** — express interest on their card in `/match browse` for a mutual match\n");
            if (i > 15) {
                break;
            }
        }
        e.replyEmbeds(MatchEmbeds.simple("Incoming interest", sb.toString())).setEphemeral(true).queue();
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
            e.reply(ToolSet.CP_EMJ + " " + EmptyStates.noChats()).setEphemeral(true).queue();
            return;
        }
        StringBuilder sb = new StringBuilder("Your connections:\n");
        List<ActionRow> rows = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        int i = 1;
        for (MatchConversation chat : chats) {
            String other = chat.otherParticipant(userId);
            String name = ctx.profiles().find(other).map(MatchProfile::getDisplayName).orElse(other);
            int unread = chat.unreadFor(userId);
            String badge = unread > 0 ? " 🔴" + unread : "";
            String preview = chat.getLastMessagePreview() == null || chat.getLastMessagePreview().isBlank()
                    ? ""
                    : "\n   _" + truncate(chat.getLastMessagePreview(), 60) + "_";
            sb.append(i++).append(". **").append(name).append("**").append(badge).append(" — `")
                    .append(chat.getStage()).append("` (").append(chat.getMessageCount()).append(" msgs)");
            if (chat.getChatStreakDays() > 1) {
                sb.append(" 🔥").append(chat.getChatStreakDays());
            }
            sb.append(preview).append('\n');
            buttons.add(Button.primary(
                    MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, chat.getConversationId()),
                    (unread > 0 ? "● " : "") + "Chat: " + truncate(name, 18)
            ));
            if (buttons.size() == 5) {
                rows.add(ActionRow.of(buttons));
                buttons = new ArrayList<>();
            }
        }
        if (!buttons.isEmpty()) {
            rows.add(ActionRow.of(buttons));
        }
        e.replyEmbeds(MatchEmbeds.simple("Match chats", sb.toString()))
                .setComponents(rows)
                .setEphemeral(true)
                .queue();
    }

    private void handleUndo(SlashCommandInteractionEvent e, ApplicationContext ctx, String userId) {
        e.deferReply(true).queue();
        ctx.dbExecutor().execute(() -> {
            DecisionService.DecisionResult result = ctx.decisions().undoLastSkip(userId);
            if (!result.success() || result.restoredSession() == null) {
                e.getHook().sendMessage(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
                return;
            }
            Optional<MatchProfile> profile = ctx.profiles().find(result.restoredSession().subjectId());
            if (profile.isEmpty()) {
                e.getHook().sendMessage(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
                return;
            }
            String sessionId = result.restoredSession().sessionId();
            e.getHook().sendMessageEmbeds(MatchEmbeds.profileCard(profile.get(), false))
                    .setContent(ToolSet.CP_EMJ + " " + result.message())
                    .addComponents(ActionRow.of(
                            Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, sessionId), "Interested"),
                            Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, sessionId), "Skip")
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
                e.reply(ToolSet.CP_EMJ + " User blocked for Match.").setEphemeral(true).queue();
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
                e.reply(ToolSet.CP_EMJ + " Report submitted (" + cat.label()
                        + "). Mods review reports; urgent categories may auto-pause the other profile pending review.")
                        .setEphemeral(true).queue();
            }
            case "unmatch" -> {
                if (target == null && conversationId == null) {
                    e.reply("Provide conversation_id (or user_id as conversation id).").setEphemeral(true).queue();
                    return;
                }
                reply(e, ctx.conversations().unmatch(userId, conversationId != null ? conversationId : target));
            }
            default -> e.replyEmbeds(MatchEmbeds.simple(
                    "Match safety",
                    """
                            `/match safety action:block user_id:<id>`
                            `/match safety action:report user_id:<id> category:<cat> reason:<text> conversation_id:<opt>`
                            `/match safety action:unmatch conversation_id:<id>`

                            Categories: harassment, spam, inappropriate, age_lie, grooming, threats, contact, impersonation, other
                            Urgent categories auto-pause the reported profile for mod review.
                            """
            )).setEphemeral(true).queue();
        }
    }

    private static void reply(SlashCommandInteractionEvent e, EnrollmentService.ServiceResult result) {
        e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
    }

    /**
     * One form for the whole profile (Discord max 5 inputs).
     * Guided join opens this right after age pick — no extra slash commands.
     */
    static Modal setupModal() {
        return Modal.create("m-v1-modal-setup", "Your Match profile")
                .addComponents(
                        Label.of("Display name", TextInput.create("displayName", TextInputStyle.SHORT)
                                .setPlaceholder("How you want to show up")
                                .setRequired(true).setMaxLength(32).build()),
                        Label.of("Short bio", TextInput.create("bio", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("A few friendly sentences about you")
                                .setRequired(true).setMaxLength(300).build()),
                        Label.of("Ideal Sunday", TextInput.create("prompt", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("What does a perfect Sunday look like?")
                                .setRequired(true).setMaxLength(200).build()),
                        Label.of("Interests (up to 5)", TextInput.create("interests", TextInputStyle.SHORT)
                                .setPlaceholder("gaming, music, art, hiking…")
                                .setRequired(true).setMaxLength(80).build()),
                        Label.of("Pronouns (optional)", TextInput.create("pronouns", TextInputStyle.SHORT)
                                .setPlaceholder("she/her · he/him · they/them")
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
        return "`/match join` — set up in ~30s (buttons + one form)\n" +
                "`/match browse` — discover people\n" +
                "`/match chats` · `/match likes` · `/match profile`\n" +
                "`/match safety` — block / report / unmatch\n";
    }

    @Override
    public String getName() {
        return "match";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Callerphone Social — discover and connect")
                .addSubcommands(
                        new SubcommandData("join", "Opt into Social matching"),
                        new SubcommandData("profile", "View your profile card"),
                        new SubcommandData("edit", "Edit your whole profile (one form)"),
                        new SubcommandData("browse", "Discover one compatible profile"),
                        new SubcommandData("likes", "See who expressed interest in you"),
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
                        new SubcommandData("premium", "Premium benefits (purchases not live)"),
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
