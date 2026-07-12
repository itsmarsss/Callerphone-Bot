package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.call.discord.CallComponentIds;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.CopyCatalog;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.experience.ViewField;
import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.EmptyStates;
import com.itsmarsss.callerphone.match.service.MatchLimits;
import com.itsmarsss.callerphone.msginbottle.BottleComponentIds;

import java.util.ArrayList;
import java.util.List;

/** Match domain states → ExperienceView. */
public final class MatchPresenter {
    private MatchPresenter() {
    }

    /**
     * Plan patterns §2 state priority:
     * NOT_ENROLLED → INCOMPLETE → PAUSED → UNREAD → INTEREST → NORMAL → DAILY_LIMIT
     */
    public static ExperienceView home(
            String displayName,
            int unreadChats,
            long discoveriesLeft,
            boolean profileLive,
            boolean enrolled
    ) {
        return home(displayName, unreadChats, discoveriesLeft, profileLive, enrolled, false, 0, 0);
    }

    public static ExperienceView home(
            String displayName,
            int unreadChats,
            long discoveriesLeft,
            boolean profileLive,
            boolean enrolled,
            boolean paused,
            int incomingInterest,
            int inboxUnread
    ) {
        if (!enrolled) {
            return ExperienceView.builder(ExperienceIntent.SOCIAL)
                    .title("Callerphone")
                    .description("Meet someone new or pick up where you left off.")
                    .actions(ActionSpec.success(
                            MatchComponentIds.of(MatchComponentIds.ACTION_JOIN_ACCEPT, "_"),
                            "Create profile"
                    ))
                    .ephemeral(true)
                    .build();
        }
        if (!profileLive && !paused) {
            return ExperienceView.builder(ExperienceIntent.PROGRESS)
                    .title(greeting(displayName))
                    .description("Finish your profile so you can appear in Discover.")
                    .actions(
                            ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Finish setup"),
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover")
                    )
                    .ephemeral(true)
                    .build();
        }
        if (paused) {
            return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                    .title(greeting(displayName))
                    .description("Your profile is paused. Chats stay available.")
                    .actions(
                            ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_RESUME, "_"), "Resume profile"),
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats")
                    )
                    .ephemeral(true)
                    .build();
        }
        if (unreadChats > 0) {
            return ExperienceView.builder(ExperienceIntent.SOCIAL)
                    .title(greeting(displayName))
                    .description(unreadChats + " unread chat" + (unreadChats == 1 ? "" : "s")
                            + (inboxUnread > 0 ? " · " + inboxUnread + " inbox" : "")
                            + " · " + discoveriesLeft + " discoveries left")
                    .actions(
                            ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"), "Open inbox"),
                            ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover people")
                    )
                    .ephemeral(true)
                    .build();
        }
        // Plan §10.F — unread human events make inbox the primary home action
        if (inboxUnread > 0) {
            return ExperienceView.builder(ExperienceIntent.SOCIAL)
                    .title(greeting(displayName))
                    .description(inboxUnread + " unread update" + (inboxUnread == 1 ? "" : "s")
                            + (incomingInterest > 0 ? " · someone is interested" : "")
                            + " · " + discoveriesLeft + " discoveries left")
                    .actions(
                            ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"), "Open inbox"),
                            ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover people"),
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats")
                    )
                    .ephemeral(true)
                    .build();
        }
        if (incomingInterest > 0) {
            return ExperienceView.builder(ExperienceIntent.SOCIAL)
                    .title(greeting(displayName))
                    .description("Someone is interested. Keep discovering — if you're interested too, you'll connect.")
                    .actions(
                            ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Keep discovering"),
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_LIKES, "_"), "Incoming interest")
                    )
                    .ephemeral(true)
                    .build();
        }
        if (discoveriesLeft <= 0) {
            return dailyLimitHome(displayName);
        }

        List<String> bits = new ArrayList<>();
        bits.add(discoveriesLeft + " discoveries left today");
        if (inboxUnread > 0) {
            bits.add(inboxUnread + " inbox");
        }
        // Plan §10.G — explore modes without becoming a stats dashboard
        // Discord allows 5 buttons/row; renderer wraps to a second row for profile/premium.
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title(greeting(displayName))
                .description(String.join(" · ", bits))
                .actions(
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover people"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.secondary(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PREVIEW_SELF, "_"), "My profile")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView inbox(List<String> lines) {
        String body = lines == null || lines.isEmpty()
                ? "You're all caught up. New connection messages, bottle replies, and games show here."
                : String.join("\n\n", lines);
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Your inbox")
                .description(body)
                .actions(
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_INBOX_OPEN, "_"), "Open next"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView emptyDiscoverWithFallback(String message) {
        String m = message == null ? "" : message;
        String lower = m.toLowerCase();
        // Daily discovery limit uses premium-aware copy from UpsellCopy
        if (lower.contains("discoveries are done")) {
            return dailyLimitHome(null);
        }
        if (lower.contains("create a profile") || lower.contains("finish your profile")) {
            return ExperienceView.builder(ExperienceIntent.PROGRESS)
                    .title("Almost there")
                    .description(m + "\n\nOr explore Callerphone while you set up.")
                    .actions(
                            ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_JOIN_ACCEPT, "_"), "Create profile"),
                            ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Finish setup"),
                            ActionSpec.secondary(CallComponentIds.again("_"), "Start a call"),
                            ActionSpec.secondary(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle")
                    )
                    .ephemeral(true)
                    .build();
        }
        if (lower.contains("paused on your account") || lower.contains("restricted")) {
            return ExperienceView.builder(ExperienceIntent.SAFETY)
                    .title("Discover paused")
                    .description(m)
                    .actions(
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                            ActionSpec.secondary(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle")
                    )
                    .ephemeral(true)
                    .build();
        }
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("You're caught up")
                .description((m.isBlank() ? EmptyStates.noCandidates() : m)
                        + "\n\nWant something else while you wait?")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit profile")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView dailyLimitHome(String displayName) {
        return ExperienceView.builder(ExperienceIntent.PREMIUM)
                .title(displayName == null || displayName.isBlank() ? "Today's discoveries are done" : greeting(displayName))
                .description(
                        "Your free Discover set refreshes tomorrow.\n\n"
                                + "Premium (later) raises daily limits — Discover stays free either way.\n\n"
                                + "Meanwhile: chats, calls, and bottles are still open."
                )
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PREMIUM, "_"), "About Premium")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView softLimit(String title, String body) {
        return ExperienceView.builder(ExperienceIntent.PREMIUM)
                .title(title == null || title.isBlank() ? "Limit reached" : title)
                .description(body + "\n\nPremium (later) may raise some limits — free paths stay open.")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PREMIUM, "_"), "About Premium")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView incomingInterestFreeTeaser() {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Someone is interested")
                .description(
                        "Keep discovering — if you're interested too, you'll connect instantly.\n\n"
                                + "Seeing **who** is interested is a Premium feature (not for sale yet)."
                )
                .actions(
                        ActionSpec.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                                "Keep discovering"
                        ),
                        ActionSpec.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_PREMIUM, "_"),
                                "About Premium"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView joinIntro(String userId) {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Create your profile")
                .description(
                        "Three quick steps. You can change everything later.\n\n"
                                + "**1.** Age group\n"
                                + "**2.** About you\n"
                                + "**3.** Preview & go live"
                )
                .footer("Step 0 of 3")
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_JOIN_ACCEPT, userId),
                        "Start setup"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView ageGroup() {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Age group")
                .description("You'll only meet people in the same group.")
                .footer("Step 1 of 3")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_13_15, "_"), "13-15"),
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_16_17, "_"), "16-17"),
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_AGE_18_PLUS, "_"), "18+")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView liveReady() {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("You're live")
                .description("Your profile can now appear in Discover.")
                .footer("Step 3 of 3")
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                        "Start discovering"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView incompleteWelcome() {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Pick up where you left off")
                .description("Finish your profile so you can appear in Discover.")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Continue setup"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PHOTO_MENU, "_"), "Add photo"),
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.secondary(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView emptyDiscover(String message) {
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("You're caught up")
                .description(message == null || message.isBlank() ? EmptyStates.noCandidates() : message)
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView emptyDiscoverAfterAction(String note, String emptyMessage) {
        String body = (note == null || note.isBlank() ? "" : note + "\n\n")
                + (emptyMessage == null || emptyMessage.isBlank() ? EmptyStates.noCandidates() : emptyMessage);
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("You're caught up")
                .description(body)
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.success(BottleComponentIds.of(BottleComponentIds.ACTION_FIND, "_"), "Find a bottle"),
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView connectionGameShelf(String conversationId) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Play with your connection")
                .description("Only ready games are listed.\n\n**Tic-Tac-Toe** · Ready")
                .actions(
                        ActionSpec.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_GAME_TTT, conversationId),
                                "Play Tic-Tac-Toe"
                        ),
                        ActionSpec.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, conversationId),
                                "Back to chat"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView generalGameShelf() {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Mini games")
                .description(
                        "Games work best **during a call** or inside a Match chat.\n\n"
                                + "**Ready**\n"
                                + "• Tic-Tac-Toe\n\n"
                                + "Start a call or open a chat, then challenge from there."
                )
                .actions(
                        ActionSpec.secondary(CallComponentIds.again("_"), "Start a call"),
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover")
                )
                .build();
    }

    public static ExperienceView incomingInterestEmpty() {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Incoming interest")
                .description(EmptyStates.noLikes())
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                        "Keep discovering"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView incomingInterestList(String body) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Incoming interest")
                .description(body)
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                        "Discover"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView chatsEmpty() {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("No conversations yet")
                .description(EmptyStates.noChats())
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                        "Discover people"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView chatSelected(String name, String message, String conversationId) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Chatting with " + name)
                .description(message)
                .actions(
                        ActionSpec.primary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_GAME_TTT, conversationId),
                                "Play a game"
                        ),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_STOP_CHAT, "_"), "Stop chat"),
                        ActionSpec.danger(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_OPEN, "conversation:" + conversationId),
                                "Safety"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView connected(String peerName, String opener, String conversationId) {
        String body = "You and **" + peerName + "** are both interested.";
        if (opener != null && !opener.isBlank()) {
            body += "\n\n_" + opener + "_";
        }
        body += "\n\nSend a text DM here to talk.";
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("You connected")
                .description(body)
                .actions(
                        ActionSpec.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, conversationId),
                                "Open chat"
                        ),
                        ActionSpec.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_ICEBREAKER, conversationId),
                                "Another prompt"
                        ),
                        ActionSpec.primary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_GAME_TTT, conversationId),
                                "Play a game"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView safetyMenu(String displayName, String contextOpaque) {
        String who = displayName == null || displayName.isBlank() ? "this person" : displayName;
        return ExperienceView.builder(ExperienceIntent.SAFETY)
                .title("Safety with " + who)
                .description("Choose what you need. They won't be told who submitted a report.")
                .actions(
                        ActionSpec.danger(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_BLOCK, contextOpaque),
                                "Block"
                        ),
                        ActionSpec.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_REPORT, contextOpaque),
                                "Report"
                        ),
                        ActionSpec.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_UNMATCH, contextOpaque),
                                "Unmatch"
                        )
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView safetyBlocked(String displayName) {
        String who = displayName == null || displayName.isBlank() ? "That person" : displayName;
        return ExperienceView.builder(ExperienceIntent.SAFETY)
                .title(who + " is blocked")
                .description("Their profile and conversations are no longer available to you.")
                .ephemeral(true)
                .build();
    }

    public static ExperienceView safetyReported() {
        return ExperienceView.builder(ExperienceIntent.SAFETY)
                .title("Report received")
                .description("Evidence was preserved for review. They have not been notified.")
                .ephemeral(true)
                .build();
    }

    public static ExperienceView safetyHelp() {
        return ExperienceView.builder(ExperienceIntent.SAFETY)
                .title("Safety")
                .description(
                        "Open **Safety** from a chat, Discover card, or shared profile.\n\n"
                                + "That attaches the right person automatically. Manual IDs are for moderators only."
                )
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"),
                        "Open chats"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView previewLive(MatchProfile profile) {
        String name = profile.getDisplayName() == null || profile.getDisplayName().isBlank()
                ? "Your profile"
                : profile.getDisplayName();
        String bio = profile.getBio() == null || profile.getBio().isBlank() ? "_No bio yet_" : profile.getBio();
        List<ViewField> fields = new ArrayList<>();
        if (profile.getAgeCohort() != null) {
            fields.add(ViewField.of("Age group", profile.getAgeCohort().label()));
        }
        if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            fields.add(ViewField.block("Interests", String.join(" · ", profile.getInterests())));
        }
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("You're live")
                .description("This is how you appear in Discover.\n\n**" + name + "**\n" + bio)
                .fields(fields)
                .footer("Step 3 of 3")
                .actions(
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Start discovering"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView setupRetry(String message) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Try again")
                .description(message)
                .footer("Step 2 of 3")
                .actions(ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Retry"))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView expired() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("That expired")
                .description(CopyCatalog.expiredAction())
                .ephemeral(true)
                .build();
    }

    public static ExperienceView leaveConfirm() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Leave Discover?")
                .description("Your profile will stop appearing, but your profile and chats will remain.")
                .actions(
                        ActionSpec.danger(MatchComponentIds.of(MatchComponentIds.ACTION_LEAVE_CONFIRM, "_"), "Leave Discover"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_LEAVE_CANCEL, "_"), "Keep profile live")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView deleteConfirm() {
        return ExperienceView.builder(ExperienceIntent.SAFETY)
                .title("Delete your Social data?")
                .description("This permanently removes your profile content and discovery history. Safety records may be retained where required.")
                .actions(
                        ActionSpec.danger(MatchComponentIds.of(MatchComponentIds.ACTION_DELETE_CONFIRM, "_"), "Continue"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_DELETE_CANCEL, "_"), "Cancel")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView editMenu() {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Edit your profile")
                .description("Choose what you want to update. You can change everything later.")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "About me"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PHOTO_MENU, "_"), "Photo"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PREVIEW_SELF, "_"), "Preview"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETTINGS, "_"), "Notifications")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView settings(boolean notifications, boolean digest) {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Notifications")
                .description(
                        "Connection alerts · **" + (notifications ? "On" : "Off") + "**\n"
                                + "Weekly discovery digest · **" + (digest ? "On" : "Off") + "**\n\n"
                                + "Toggle below. Changes apply immediately."
                )
                .actions(
                        ActionSpec.primary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_TOGGLE_NOTIFY, "_"),
                                notifications ? "Turn alerts off" : "Turn alerts on"
                        ),
                        ActionSpec.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_TOGGLE_DIGEST, "_"),
                                digest ? "Turn digest off" : "Turn digest on"
                        ),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"), "Inbox")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView premiumOverview(boolean isPremium) {
        if (isPremium) {
            return ExperienceView.builder(ExperienceIntent.PREMIUM)
                    .title("Callerphone Premium")
                    .description(
                            "You're on Premium.\n\n"
                                    + "· " + MatchLimits.PREMIUM_DAILY_DISCOVERIES + " discoveries / day\n"
                                    + "· " + MatchLimits.PREMIUM_DAILY_INTERESTS + " interests / day\n"
                                    + "· " + MatchLimits.PREMIUM_ACTIVE_CONVERSATIONS + " open chats\n"
                                    + "· Incoming interest names\n"
                                    + "· Extra undos"
                    )
                    .actions(
                            ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover"),
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats")
                    )
                    .ephemeral(true)
                    .build();
        }
        return ExperienceView.builder(ExperienceIntent.PREMIUM)
                .title("Callerphone Premium")
                .description(
                        "More control over who you discover and how often you connect.\n\n"
                                + "**Free**\n"
                                + "· " + MatchLimits.FREE_DAILY_DISCOVERIES + " discoveries\n"
                                + "· " + MatchLimits.FREE_DAILY_INTERESTS + " interests\n"
                                + "· " + MatchLimits.FREE_ACTIVE_CONVERSATIONS + " open chats\n"
                                + "· Basic filters\n\n"
                                + "**Premium** *(coming later)*\n"
                                + "· " + MatchLimits.PREMIUM_DAILY_DISCOVERIES + " discoveries\n"
                                + "· " + MatchLimits.PREMIUM_DAILY_INTERESTS + " interests\n"
                                + "· Incoming interest names\n"
                                + "· Advanced filters · more undos\n\n"
                                + "Discover stays free either way. Purchases aren't live yet."
                )
                .actions(
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Keep discovering"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_LIKES, "_"), "Incoming interest")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView photoMenu() {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Profile image")
                .description(
                        "Use your Discord avatar (recommended) or attach a public **https** image URL with "
                                + "`/match photo url:`."
                )
                .actions(
                        ActionSpec.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_PHOTO_AVATAR, "_"),
                                "Use Discord avatar"
                        ),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PREVIEW_SELF, "_"), "Preview profile")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView photoUpdated() {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Image added")
                .description("Review how it looks on your profile.")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_PREVIEW_SELF, "_"), "Preview"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PHOTO_MENU, "_"), "Change image"),
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView activityProfile(
            String displayName,
            int level,
            int exp,
            String bar,
            long callMessages,
            long commands,
            boolean matchLive,
            String matchSummary
    ) {
        StringBuilder body = new StringBuilder();
        body.append(bar).append(" **").append(exp).append("/100** XP · Level **").append(level).append("**\n");
        body.append(callMessages).append(" call messages · ").append(commands).append(" commands");
        if (matchSummary != null && !matchSummary.isBlank()) {
            body.append("\n\n**Match**\n").append(matchSummary);
        } else if (!matchLive) {
            body.append("\n\n_No Match profile yet — `/match join` to meet people._");
        }
        body.append("\n\n_Levels unlock call flair over time. Credits stay secondary until rewards ship._");
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title((displayName == null || displayName.isBlank() ? "You" : displayName) + " · Level " + level)
                .description(body.toString())
                .actions(
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_EDIT_MENU, "_"), "Edit Match")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView quietSuccess(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }

    public static ExperienceView warn(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }

    public static String ageLabel(AgeCohort cohort) {
        return cohort == null ? "your group" : cohort.label();
    }

    private static String greeting(String displayName) {
        if (displayName == null || displayName.isBlank() || "Someone".equals(displayName)) {
            return "Callerphone";
        }
        return "Hi, " + displayName;
    }
}
