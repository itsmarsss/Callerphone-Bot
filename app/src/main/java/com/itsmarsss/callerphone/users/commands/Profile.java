package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.Instant;

public class Profile implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping target = e.getOption("target");
        User user = target != null ? target.getAsUser() : e.getUser();
        e.replyEmbeds(profile(user)).queue();
    }

    private MessageEmbed profile(User user) {
        String userId = user.getId();
        final long executed = Users.getExecuted(userId);
        final long transmitted = Users.getTransmitted(userId);
        final long total = executed + transmitted;
        final int level = (int) (total / 100);
        final int exp = (int) (total % 100);

        String tempPrefix = Users.getPrefix(userId);
        final String prefixDisplay = tempPrefix.isEmpty()
                ? (level >= 50 ? ":unlock: `/prefix <prefix>`" : ":lock: `Level 50`")
                : "`" + tempPrefix + "`";

        long now = System.currentTimeMillis();
        long creditElapsed = now - Cooldown.getPoolCooldown(userId);
        long commandElapsed = now - Cooldown.getCmdCooldown(userId);

        return new EmbedBuilder()
                .setTitle("**" + user.getName() + "'s Profile**")
                .setThumbnail(user.getAvatarUrl())
                .addField("**General**", String.format(Response.PROFILE_GENERAL.toString(), level, exp, prefixDisplay), true)
                .addField("**Credits**", String.format(Response.PROFILE_CREDITS.toString(), Users.getCredits(userId), 0, 0), true)
                .addField("**Messages**", String.format(Response.PROFILE_MESSAGE.toString(), executed, transmitted, total), true)
                .addField("**Credit cooldown**", ToolSet.formatCooldown(creditElapsed, ToolSet.CREDIT_COOLDOWN, "second(s)"), true)
                .addField("**Command cooldown**", ToolSet.formatCooldown(commandElapsed, ToolSet.COMMAND_COOLDOWN, "second(s)"), true)
                .addField("**Status**", Users.getUserStatus(userId), true)
                .setFooter("Profile", Callerphone.selfUser != null ? Callerphone.selfUser.getAvatarUrl() : null)
                .setTimestamp(Instant.now())
                .setColor(ToolSet.COLOR)
                .build();
    }

    @Override
    public String getHelp() {
        return "</profile:1075168888263815199> - View your profile with Callerphone.";
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .addOptions(new OptionData(OptionType.USER, "target", "Target user"))
                .setContexts(InteractionContextType.GUILD);
    }
}
