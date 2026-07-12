package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Compatibility facade — delegates to {@link CallPresenter} + {@link ExperienceRenderer}. */
public final class CallEmbeds {
    private CallEmbeds() {
    }

    public static MessageEmbed connected() {
        return ExperienceRenderer.toEmbed(ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Call connected")
                .description(
                        "Another server picked up. Messages in this channel go to them.\n\n"
                                + "Share your profile if you want to keep talking later."
                )
                .footer("End anytime with /endcall")
                .build());
    }

    public static MessageEmbed queued(int pos, int size) {
        return ExperienceRenderer.toEmbed(CallPresenter.queued(pos, size));
    }

    public static MessageEmbed waiting(int pos, int size) {
        return ExperienceRenderer.toEmbed(CallPresenter.waiting(pos, size));
    }

    public static MessageEmbed ended() {
        return ExperienceRenderer.toEmbed(CallPresenter.ended());
    }

    public static MessageEmbed peerHungUp() {
        return ExperienceRenderer.toEmbed(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Call ended")
                .description("The other side hung up.")
                .build());
    }

    public static MessageEmbed leftQueue() {
        return ExperienceRenderer.toEmbed(CallPresenter.leftQueue());
    }

    public static MessageEmbed noCall() {
        return ExperienceRenderer.toEmbed(CallPresenter.noCall());
    }

    public static MessageEmbed conflict() {
        return ExperienceRenderer.toEmbed(CallPresenter.conflict());
    }

    public static MessageEmbed info(String title, String description) {
        return ExperienceRenderer.toEmbed(CallPresenter.info(title, description));
    }

    public static MessageEmbed warn(String title, String description) {
        return ExperienceRenderer.toEmbed(CallPresenter.warn(title, description));
    }

    public static MessageEmbed success(String title, String description) {
        return ExperienceRenderer.toEmbed(CallPresenter.success(title, description));
    }

    public static MessageEmbed soft(String message) {
        return ExperienceRenderer.toEmbed(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .description(message)
                .build());
    }
}
