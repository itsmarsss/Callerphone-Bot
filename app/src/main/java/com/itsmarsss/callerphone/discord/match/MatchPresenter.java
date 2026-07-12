package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.CopyCatalog;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.experience.ViewField;
import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.EmptyStates;

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
            return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                    .title(greeting(displayName))
                    .description("Today's discoveries are done. Your free set refreshes tomorrow.")
                    .actions(
                            ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                            ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"), "Inbox")
                    )
                    .ephemeral(true)
                    .build();
        }

        List<String> bits = new ArrayList<>();
        bits.add(discoveriesLeft + " discoveries left today");
        if (inboxUnread > 0) {
            bits.add(inboxUnread + " inbox");
        }
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title(greeting(displayName))
                .description(String.join(" · ", bits))
                .actions(
                        ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover people"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"), "Inbox")
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
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("You're caught up")
                .description((message == null || message.isBlank() ? EmptyStates.noCandidates() : message)
                        + "\n\nWant something else while you wait?")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit profile")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView incomingInterestFreeTeaser() {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Someone is interested")
                .description("Keep discovering — if you're interested too, you'll connect instantly.")
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                        "Keep discovering"
                ))
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
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover")
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
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit profile")
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
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit profile")
                )
                .ephemeral(true)
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
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("You connected")
                .description(body)
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, conversationId),
                        "Open chat"
                ))
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
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_PREVIEW_SELF, "_"), "Preview")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView settings(boolean notifications, boolean digest) {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Notifications")
                .description(
                        "Connection alerts · " + (notifications ? "On" : "Off") + "\n"
                                + "Weekly discovery digest · " + (digest ? "On" : "Off") + "\n\n"
                                + "Use `/match notify` and `/match digest` to change these."
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
