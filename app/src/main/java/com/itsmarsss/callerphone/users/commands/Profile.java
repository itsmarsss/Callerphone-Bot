package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.Instant;

/**
 * Lightweight activity card. Credits, cooldowns, and locked rewards stay out of the
 * primary surface until real unlocks exist.
 */
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
        int filled = Math.max(0, Math.min(10, exp / 10));
        String bar = "█".repeat(filled) + "░".repeat(10 - filled);

        String prefix = Users.getPrefix(userId);
        String prefixLine = prefix == null || prefix.isEmpty() ? "" : "Prefix `" + prefix + "`\n";

        return new EmbedBuilder()
                .setTitle(user.getName() + " · Level " + level)
                .setThumbnail(user.getAvatarUrl())
                .setDescription(prefixLine + bar + " " + exp + "/100 XP\n"
                        + transmitted + " call messages · " + executed + " commands")
                .setFooter("Callerphone", Callerphone.selfUser != null ? Callerphone.selfUser.getAvatarUrl() : null)
                .setTimestamp(Instant.now())
                .setColor(ToolSet.COLOR)
                .build();
    }

    @Override
    public String getHelp() {
        return "`/profile` view activity and level";
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "View activity and level")
                .addOptions(new OptionData(OptionType.USER, "target", "Target user"))
                .setContexts(InteractionContextType.GUILD);
    }
}
