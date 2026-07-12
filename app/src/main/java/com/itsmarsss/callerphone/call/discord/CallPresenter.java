package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.model.CallSession;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.experience.ViewField;

/** Domain call state → ExperienceView. */
public final class CallPresenter {
    private CallPresenter() {
    }

    public static ExperienceView queued(int position, int size) {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Finding a call")
                .description("You're in line. We'll connect this channel automatically.")
                .fields(java.util.List.of(ViewField.of("Queue", "#" + position + " of " + size)))
                .footer("Leave with /endcall")
                .build();
    }

    public static ExperienceView waiting(int position, int size) {
        return ExperienceView.builder(ExperienceIntent.PROGRESS)
                .title("Still finding a call")
                .description("You're still in line. We'll update this channel when someone connects.")
                .fields(java.util.List.of(ViewField.of("Queue", "#" + position + " of " + size)))
                .footer("Leave with /endcall")
                .build();
    }

    public static ExperienceView connected(CallSession session) {
        return connected(session.getId());
    }

    public static ExperienceView connected(String sessionId) {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Call connected")
                .description(
                        "Another server picked up. Messages in this channel go to them.\n\n"
                                + "Share your profile if you want to keep talking later."
                )
                .footer("End anytime with /endcall")
                .actions(
                        ActionSpec.primary(CallComponentIds.share(sessionId), "Share profile"),
                        ActionSpec.danger(CallComponentIds.report(sessionId), "Report")
                )
                .build();
    }

    public static ExperienceView ended(String sessionId) {
        ExperienceView.Builder b = ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Call ended")
                .description("Thanks for chatting.");
        if (sessionId != null && !sessionId.isBlank()) {
            b.actions(ActionSpec.danger(CallComponentIds.report(sessionId), "Report"));
        }
        return b.build();
    }

    public static ExperienceView ended() {
        return ended(null);
    }

    public static ExperienceView peerHungUp(String sessionId) {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Call ended")
                .description("The other side hung up.")
                .actions(ActionSpec.danger(CallComponentIds.report(sessionId), "Report"))
                .build();
    }

    public static ExperienceView leftQueue() {
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Left the queue")
                .description("This channel is no longer waiting for a call.")
                .build();
    }

    public static ExperienceView noCall() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("No active call")
                .description("Start a random conversation with another server.")
                .build();
    }

    public static ExperienceView conflict() {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title("Already on a call")
                .description("This channel is already connected. End it before starting another.")
                .build();
    }

    public static ExperienceView warn(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.WARNING)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }

    public static ExperienceView success(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }

    public static ExperienceView info(String title, String description) {
        return ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title(title)
                .description(description)
                .ephemeral(true)
                .build();
    }
}
