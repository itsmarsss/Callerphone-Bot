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

    public static ExperienceView home(
            String displayName,
            int unreadChats,
            long discoveriesLeft,
            boolean profileLive,
            boolean enrolled
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
        if (!profileLive) {
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

        List<String> bits = new ArrayList<>();
        if (unreadChats > 0) {
            bits.add(unreadChats + " unread chat" + (unreadChats == 1 ? "" : "s"));
        }
        bits.add(discoveriesLeft + " discoveries left today");

        ActionSpec primary = unreadChats > 0
                ? ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats")
                : ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover people");
        ActionSpec secondary = unreadChats > 0
                ? ActionSpec.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover people")
                : ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"), "Open chats");

        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title(greeting(displayName))
                .description(String.join(" · ", bits))
                .actions(primary, secondary)
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
