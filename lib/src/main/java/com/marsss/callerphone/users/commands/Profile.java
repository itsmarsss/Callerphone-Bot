package com.marsss.callerphone.users.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class Profile implements ICommand {
    @Override
    public void runCommand(MessageReceivedEvent e) {
        final List<User> MENTIONS = e.getMessage().getMentionedUsers();
        final User USER = MENTIONS.size() > 0 ? MENTIONS.get(0) : e.getAuthor();

        e.getMessage().replyEmbeds(profile(USER)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.replyEmbeds(profile(e.getOption("target").getAsUser())).queue();
    }

    private MessageEmbed profile(User user) {
        final long EXECUTED = Storage.getExecuted(user);
        final long TRANSMITTED = Storage.getTransmitted(user);
        final long TOTAL = EXECUTED + TRANSMITTED;
        final int LVL = (int) TOTAL / 100;
        final int EXP = (int) TOTAL - 100 * LVL;
        String tempPrefix = Storage.getPrefix(user.getId());
        final String PREFIX = (tempPrefix.equals("") ? (LVL > 5 ? ":unlock: `" + Callerphone.config.getPrefix() + "prefix <prefix>`" : ":lock: `Level 50`") : tempPrefix);

        String general = String.format(Response.PROFILE_GENERAL.toString(), LVL, EXP, PREFIX);
        String credits = String.format(Response.PROFILE_CREDITS.toString(), Storage.getCredits(user), 0, 0);
        String message = String.format(Response.PROFILE_MESSAGE.toString(), EXECUTED, TRANSMITTED, TOTAL);

        EmbedBuilder proEmd = new EmbedBuilder()
                .setTitle("**" + user.getName() + "'s Profile**")
                .setThumbnail(user.getAvatarUrl())
                .addField("**General**", general, true)
                .addField("**Credits**", credits, true)
                .addField("**Messages**", message, true)
                .addField("**Credit cooldown**", ((System.currentTimeMillis() - Storage.getUserCooldown(user)) < ToolSet.CREDIT_COOLDOWN ? ":alarm_clock: " + ((ToolSet.CREDIT_COOLDOWN - (System.currentTimeMillis() - Storage.getUserCooldown(user))) / 1000) + " second(s)" : ":white_check_mark: None"), true)
                .addField("**Command cooldown**", ((System.currentTimeMillis() - Storage.getCmdCooldown(user)) < ToolSet.COMMAND_COOLDOWN ? ":alarm_clock: " + ((ToolSet.COMMAND_COOLDOWN - (System.currentTimeMillis() - Storage.getCmdCooldown(user))) / 1000) + " second(s)" : ":white_check_mark: None"), true)
                .addField("**Status**", Storage.getStatus(user.getId()), true)
                .setFooter("Profile", Callerphone.jda.getSelfUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .setColor(new Color(114, 137, 218));

        return proEmd.build();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "profile` - View your profile with Callerphone.";
    }

    @Override
    public String[] getTriggers() {
        return "profile,me,myself,aboutme,myprofile,stats".split(",");
    }
}
