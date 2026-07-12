package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.experience.ProgressBar;
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
 * Plan §21: activity-first profile. Credits/unlocks stay secondary until the economy has real value.
 */
public class Profile implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping target = e.getOption("target");
        User user = target != null ? target.getAsUser() : e.getUser();
        boolean self = user.getId().equals(e.getUser().getId());
        e.replyEmbeds(profile(user, self)).queue();
    }

    private MessageEmbed profile(User user, boolean self) {
        String userId = user.getId();
        final long executed = Users.getExecuted(userId);
        final long transmitted = Users.getTransmitted(userId);
        final long total = executed + transmitted;
        final int level = (int) (total / 100);
        final int exp = (int) (total % 100);
        String bar = ProgressBar.of(exp, 100, 10);

        String prefix = Users.getPrefix(userId);
        StringBuilder desc = new StringBuilder();
        desc.append(bar).append(" **").append(exp).append("/100** XP\n");
        desc.append(transmitted).append(" call messages · ").append(executed).append(" commands");
        if (prefix != null && !prefix.isEmpty()) {
            desc.append("\nPrefix `").append(prefix).append("`");
        }
        if (self) {
            desc.append("\n\n_Levels unlock call flair over time. Credits stay in the background until rewards ship._");
        }

        return new EmbedBuilder()
                .setTitle(user.getName() + " · Level " + level)
                .setThumbnail(user.getAvatarUrl())
                .setDescription(desc.toString())
                .setColor(ToolSet.COLOR)
                .setFooter("Callerphone", Callerphone.selfUser != null ? Callerphone.selfUser.getAvatarUrl() : null)
                .setTimestamp(Instant.now())
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
