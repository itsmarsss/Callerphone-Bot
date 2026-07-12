package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

/** Call lifecycle embeds (will migrate to ExperienceView). */
public final class CallEmbeds {
    private static final Color SOCIAL = new Color(88, 101, 242);
    private static final Color SUCCESS = new Color(87, 242, 135);
    private static final Color PROGRESS = new Color(59, 130, 246);
    private static final Color WARNING = new Color(255, 183, 77);
    private static final Color NEUTRAL = new Color(155, 163, 175);

    private CallEmbeds() {
    }

    public static MessageEmbed connected() {
        return new EmbedBuilder()
                .setColor(SUCCESS)
                .setTitle("Call connected")
                .setDescription(
                        "Another server picked up. Messages in this channel go to them.\n\n"
                                + "Share your profile if you want to keep talking later."
                )
                .setFooter("End anytime with /endcall")
                .build();
    }

    public static MessageEmbed queued(int pos, int size) {
        return new EmbedBuilder()
                .setColor(PROGRESS)
                .setTitle("Finding a call")
                .setDescription("You're in line. We'll connect this channel automatically.")
                .addField("Queue", "#" + pos + " of " + size, true)
                .setFooter("Leave with /endcall")
                .build();
    }

    public static MessageEmbed waiting(int pos, int size) {
        return new EmbedBuilder()
                .setColor(PROGRESS)
                .setTitle("Still finding a call")
                .setDescription("You're still in line. We'll update this channel when someone connects.")
                .addField("Queue", "#" + pos + " of " + size, true)
                .setFooter("Leave with /endcall")
                .build();
    }

    public static MessageEmbed ended() {
        return new EmbedBuilder()
                .setColor(NEUTRAL)
                .setTitle("Call ended")
                .setDescription("Thanks for chatting.")
                .build();
    }

    public static MessageEmbed peerHungUp() {
        return new EmbedBuilder()
                .setColor(NEUTRAL)
                .setTitle("Call ended")
                .setDescription("The other side hung up.")
                .build();
    }

    public static MessageEmbed leftQueue() {
        return new EmbedBuilder()
                .setColor(NEUTRAL)
                .setTitle("Left the queue")
                .setDescription("This channel is no longer waiting for a call.")
                .build();
    }

    public static MessageEmbed noCall() {
        return new EmbedBuilder()
                .setColor(WARNING)
                .setTitle("No active call")
                .setDescription("Start a random conversation with another server.")
                .build();
    }

    public static MessageEmbed conflict() {
        return new EmbedBuilder()
                .setColor(WARNING)
                .setTitle("Already on a call")
                .setDescription("This channel is already connected. End it before starting another.")
                .build();
    }

    public static MessageEmbed info(String title, String description) {
        return base(SOCIAL, title, description);
    }

    public static MessageEmbed warn(String title, String description) {
        return base(WARNING, title, description);
    }

    public static MessageEmbed success(String title, String description) {
        return base(SUCCESS, title, description);
    }

    public static MessageEmbed soft(String message) {
        return new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setDescription(message)
                .build();
    }

    private static MessageEmbed base(Color color, String title, String description) {
        EmbedBuilder emb = new EmbedBuilder().setColor(color).setTitle(title);
        if (description != null && !description.isBlank()) {
            emb.setDescription(description);
        }
        return emb.build();
    }
}
