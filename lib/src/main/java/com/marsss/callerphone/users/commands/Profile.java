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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.Instant;

public class Profile implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        User user = e.getUser();

        if(!e.getOptions().isEmpty()) {
            user = e.getOptions().get(0).getAsUser();
        }

        e.replyEmbeds(profile(user)).queue();
    }

    private MessageEmbed profile(User user) {
        final long EXECUTED = Users.getExecuted(user.getId());
        final long TRANSMITTED = Users.getTransmitted(user.getId());
        final long TOTAL = EXECUTED + TRANSMITTED;
        final int LVL = (int) TOTAL / 100;
        final int EXP = (int) TOTAL - 100 * LVL;
        String tempPrefix = Users.getPrefix(user.getId());
        final String PREFIX = (tempPrefix.isEmpty() ? (LVL > 10 ? ":unlock: `/prefix <prefix>`" : ":lock: `Level 10`") : "`" + tempPrefix + "`");

        String general = String.format(Response.PROFILE_GENERAL.toString(), LVL, EXP, PREFIX);
        String credits = String.format(Response.PROFILE_CREDITS.toString(), Users.getCredits(user.getId()), 0, 0);
        String message = String.format(Response.PROFILE_MESSAGE.toString(), EXECUTED, TRANSMITTED, TOTAL);

        EmbedBuilder profileEmbed = new EmbedBuilder()
                .setTitle("**" + user.getName() + "'s Profile**")
                .setThumbnail(user.getAvatarUrl())
                .addField("**General**", general, true)
                .addField("**Credits**", credits, true)
                .addField("**Messages**", message, true)
                .addField("**Credit cooldown**", (getCreditCooldown(user)), true)
                .addField("**Command cooldown**", (getCommandCooldown(user)), true)
                .addField("**Status**", Users.getUserStatus(user.getId()), true)
                .setFooter("Profile", Callerphone.selfUser.getAvatarUrl())
                .setTimestamp(Instant.now())
                .setColor(ToolSet.COLOR);

        return profileEmbed.build();
    }

    private String getCreditCooldown(User user) {
        return (System.currentTimeMillis() - Cooldown.getPoolCooldown(user.getId())) < ToolSet.CREDIT_COOLDOWN ?
                ":alarm_clock: " + ((ToolSet.CREDIT_COOLDOWN - (System.currentTimeMillis() - Cooldown.getPoolCooldown(user.getId()))) / 1000) + " second(s)" :
                ":white_check_mark: None";
    }

    private String getCommandCooldown(User user) {
        return (System.currentTimeMillis() - Cooldown.getCmdCooldown(user.getId())) < ToolSet.COMMAND_COOLDOWN ?
                ":alarm_clock: " + ((ToolSet.COMMAND_COOLDOWN - (System.currentTimeMillis() - Cooldown.getCmdCooldown(user.getId()))) / 1000) + " second(s)" :
                ":white_check_mark: None";
    }

    @Override
    public String getHelp() {
        return "</profile:1075168888263815199> - View your profile with Callerphone.";
    }

    @Override
    public String[] getTriggers() {
        return "profile,me,myself,aboutme,myprofile,stats".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1]).addOptions(
                        new OptionData(OptionType.USER, "target", "Target user")
                )
                .setGuildOnly(true);
    }
}
