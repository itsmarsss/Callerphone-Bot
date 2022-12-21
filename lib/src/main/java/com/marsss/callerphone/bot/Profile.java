package com.marsss.callerphone.bot;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;

public class Profile implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().replyEmbeds(profile(e.getAuthor())).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.replyEmbeds(profile(e.getUser())).queue();
    }

    private final String GENERAL = "Level: `%s`\nExperience: `%s`\nPrefix: `%s`";
    private final String CREDITS = "Credits: `\u00A9%s`\nRedeemed: `\u00A9%s`\nNet: `\u00A9%s`";
    private final String MESSAGE = "Executed: `%s`\n Transmitted: `%s`";

    private MessageEmbed profile(User user) {

        String general = String.format(GENERAL);
        String credits = String.format(CREDITS);
        String message = String.format(MESSAGE);

        EmbedBuilder proEmd = new EmbedBuilder()
                .setTitle("**" + user.getName() + "'s Profile**")
                .setThumbnail(user.getAvatarUrl())
                .addField("**General**", general, true)
                .addField("**Credits**", credits, true)
                .addField("**Messages**", message, true)
                .setFooter("Your Profile", "https://cdn.discordapp.com/emojis/899042768394006540.webp")
                .setTimestamp(Instant.now())
                .setColor(new Color(114, 137, 218));

        return proEmd.build();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "profile` - View your profile with Callerphone.";
    }

    @Override
    public String[] getTriggers() {
        return "profile,me,myself,aboutme,myprofile,stats".split(",");
    }
}
