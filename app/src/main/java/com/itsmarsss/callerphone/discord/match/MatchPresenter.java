package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.CopyCatalog;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.EmptyStates;

/** Match domain states → ExperienceView. */
public final class MatchPresenter {
    private MatchPresenter() {
    }

    public static ExperienceView joinIntro(String userId) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Create your profile")
                .description(
                        "Meet people your age. Friendship first.\n\n"
                                + "Agree, pick an age group, then one short form. You can change everything later."
                )
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_JOIN_ACCEPT, userId),
                        "Let's go"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView liveReady() {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("You're live")
                .description("Your profile can now appear in Discover.")
                .actions(ActionSpec.success(
                        MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                        "Discover"
                ))
                .ephemeral(true)
                .build();
    }

    public static ExperienceView incompleteWelcome() {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Welcome back")
                .description("Finish your profile so you can appear in Discover.")
                .actions(
                        ActionSpec.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Continue"),
                        ActionSpec.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover")
                )
                .ephemeral(true)
                .build();
    }

    public static ExperienceView emptyDiscover(String message) {
        return ExperienceView.builder(ExperienceIntent.DISCOVERY)
                .title("You're caught up")
                .description(message == null || message.isBlank() ? EmptyStates.noCandidates() : message)
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

    public static ExperienceView expired() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("That expired")
                .description(CopyCatalog.expiredAction())
                .ephemeral(true)
                .build();
    }

    public static ExperienceView profileCardShell(MatchProfile profile, boolean self) {
        // Full card still uses MatchEmbeds until media/fields fully migrate.
        return ExperienceView.builder(self ? ExperienceIntent.SOCIAL : ExperienceIntent.DISCOVERY)
                .title(self ? profile.getDisplayName() : "Meet " + profile.getDisplayName())
                .description(profile.getBio())
                .ephemeral(true)
                .build();
    }
}
