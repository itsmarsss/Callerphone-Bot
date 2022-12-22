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

    private final String GENERAL = "Level: `%d`\nExperience: `%d/100`\nPrefix: %s\n\n[`c?help exp`]";
    private final String CREDITS = "Credits: `\u00A9 %d`\nRedeemed: `\u00A9 %d`\nNet: `\u00A9 %d`\n\n[`c?help creds`]";
    private final String MESSAGE = "Executed: `%d`\n Transmitted: `%s`\nTotal: `%s`";

    private MessageEmbed profile(User user) {
        final long EXECUTED = Callerphone.getExecuted(user);
        final long TRANSMITTED = Callerphone.getTransmitted(user);
        final long TOTAL = EXECUTED + TRANSMITTED;
        final int LVL = (TOTAL >= 100 ? (int) TOTAL % 100 : 0);
        final int EXP = (int) TOTAL - 100 * LVL;
        final String PREFIX = Callerphone.prefix.getOrDefault(user.getId(), (LVL > 5 ? ":unlock: `c?prefix <prefix>`" : ":lock: `Level 5`"));

        String general = String.format(GENERAL, LVL, EXP, PREFIX);
        String credits = String.format(CREDITS, Callerphone.getCredits(user), 0, 0);
        String message = String.format(MESSAGE, EXECUTED, TRANSMITTED, TOTAL);

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
