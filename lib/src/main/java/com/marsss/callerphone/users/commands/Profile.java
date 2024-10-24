package com.marsss.callerphone.users.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.ISlashCommand;
import com.marsss.database.categories.Cooldown;
import com.marsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;

public class Profile implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.replyEmbeds(profile(e.getOption("target").getAsUser())).queue();
    }

    private MessageEmbed profile(User user) {
        final long EXECUTED = Users.getExecuted(user.getId());
        final long TRANSMITTED = Users.getTransmitted(user.getId());
        final long TOTAL = EXECUTED + TRANSMITTED;
        final int LVL = (int) TOTAL / 100;
        final int EXP = (int) TOTAL - 100 * LVL;
        String tempPrefix = Users.getPrefix(user.getId());
        final String PREFIX = (tempPrefix.equals("") ? (LVL > 5 ? ":unlock: `" + Callerphone.config.getPrefix() + "prefix <prefix>`" : ":lock: `Level 50`") : tempPrefix);

        String general = String.format(Response.PROFILE_GENERAL.toString(), LVL, EXP, PREFIX);
        String credits = String.format(Response.PROFILE_CREDITS.toString(), Users.getCredits(user.getId()), 0, 0);
        String message = String.format(Response.PROFILE_MESSAGE.toString(), EXECUTED, TRANSMITTED, TOTAL);

        EmbedBuilder proEmd = new EmbedBuilder()
                .setTitle("**" + user.getName() + "'s Profile**")
                .setThumbnail(user.getAvatarUrl())
                .addField("**General**", general, true)
                .addField("**Credits**", credits, true)
                .addField("**Messages**", message, true)
                .addField("**Credit cooldown**", (getCreditCooldown(user)), true)
                .addField("**Command cooldown**", (getCommandCooldown(user)), true)
                .addField("**Status**", Users.getUserStatus(user.getId()), true)
                .setFooter("Profile", Callerphone.jda.getSelfUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .setColor(new Color(114, 137, 218));

        return proEmd.build();
    }

    private String getCreditCooldown(User user) {
        return (System.currentTimeMillis() - Cooldown.queryUserCooldown(user.getId())) < ToolSet.CREDIT_COOLDOWN ?
                ":alarm_clock: " + ((ToolSet.CREDIT_COOLDOWN - (System.currentTimeMillis() - Cooldown.queryUserCooldown(user.getId()))) / 1000) + " second(s)" :
                ":white_check_mark: None";
    }

    private String getCommandCooldown(User user) {
        return (System.currentTimeMillis() - Cooldown.getCmdCooldown(user.getId())) < ToolSet.COMMAND_COOLDOWN ?
                ":alarm_clock: " + ((ToolSet.COMMAND_COOLDOWN - (System.currentTimeMillis() - Cooldown.getCmdCooldown(user.getId()))) / 1000) + " second(s)" :
                ":white_check_mark: None";
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
