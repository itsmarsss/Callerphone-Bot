package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.discord.match.MatchPresenter;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ProgressBar;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
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
 * Plan §21: activity-first profile. Match summary for self when enrolled.
 */
public class Profile implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping target = e.getOption("target");
        User user = target != null ? target.getAsUser() : e.getUser();
        boolean self = user.getId().equals(e.getUser().getId());

        if (self && ApplicationContext.isReady()) {
            e.reply(ExperienceRenderer.toMessage(selfActivityView(user))).setEphemeral(true).queue();
            return;
        }
        e.replyEmbeds(legacyProfile(user, self)).queue();
    }

    private static com.itsmarsss.callerphone.experience.ExperienceView selfActivityView(User user) {
        String userId = user.getId();
        final long executed = Users.getExecuted(userId);
        final long transmitted = Users.getTransmitted(userId);
        final long total = executed + transmitted;
        final int level = (int) (total / 100);
        final int exp = (int) (total % 100);
        String bar = ProgressBar.of(exp, 100, 10);

        String matchSummary = null;
        boolean live = false;
        try {
            ApplicationContext ctx = ApplicationContext.get();
            var profile = ctx.profiles().find(userId);
            if (profile.isPresent()) {
                MatchProfile p = profile.get();
                live = p.getState() == ProfileState.ACTIVE;
                String name = p.getDisplayName() == null || p.getDisplayName().isBlank()
                        ? "Profile"
                        : p.getDisplayName();
                String state = p.getState() == null ? "draft" : p.getState().name().toLowerCase().replace('_', ' ');
                matchSummary = "**" + name + "** · " + state;
                if (p.getAgeCohort() != null) {
                    matchSummary += " · " + p.getAgeCohort().label();
                }
                int chats = ctx.conversations().list(userId).size();
                matchSummary += " · " + chats + " chat" + (chats == 1 ? "" : "s");
            }
        } catch (Exception ignored) {
        }

        return MatchPresenter.activityProfile(
                user.getName(),
                level,
                exp,
                bar,
                transmitted,
                executed,
                live,
                matchSummary
        );
    }

    private MessageEmbed legacyProfile(User user, boolean self) {
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
            desc.append("\n\n_Levels unlock call flair over time._");
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
        return "`/profile` view activity and Match summary";
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "View activity and level")
                .addOptions(new OptionData(OptionType.USER, "target", "Target user"))
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
