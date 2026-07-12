package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Plan §20: level-gated call prefix with preview. */
public final class PrefixCommand implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String userId = e.getUser().getId();
        long level = (Users.getExecuted(userId) + Users.getTransmitted(userId)) / 100;
        OptionMapping option = e.getOption("prefix");

        if (level < Constants.PREFIX_MIN_LEVEL) {
            long need = Constants.PREFIX_MIN_LEVEL - level;
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.PROGRESS)
                    .title("Prefix unlocks at Level " + Constants.PREFIX_MIN_LEVEL)
                    .description(
                            "You're Level **" + level + "** · **" + need + "** levels to go.\n\n"
                                    + "Earn XP from calls and commands — hang out, then come back."
                    )
                    .actions(
                            com.itsmarsss.callerphone.experience.ActionSpec.primary(
                                    CallComponentIds.again("_"),
                                    "Start a call"
                            ),
                            com.itsmarsss.callerphone.experience.ActionSpec.success(
                                    com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                            com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE,
                                            "_"
                                    ),
                                    "Discover people"
                            )
                    )
                    .build())).setEphemeral(true).queue();
            return;
        }

        if (option == null) {
            String current = Users.getPrefix(userId);
            if (current == null || current.isEmpty()) {
                e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                        .title("Call prefix")
                        .description("No prefix set. Pass a short tag with `/prefix` to add one.\n\n"
                                + "Preview: **[YourTag] " + e.getUser().getName() + "** · Hello")
                        .build())).setEphemeral(true).queue();
                return;
            }
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                    .title("Call prefix")
                    .description("Your messages appear as:\n**[" + current + "] " + e.getUser().getName() + "** · Hello")
                    .build())).setEphemeral(true).queue();
            return;
        }

        String prefix = option.getAsString().trim();
        if (prefix.isEmpty() || prefix.length() > Constants.PREFIX_MAX_LENGTH) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                    .title("Too long")
                    .description("Prefix max length is " + Constants.PREFIX_MAX_LENGTH + " characters.")
                    .build())).setEphemeral(true).queue();
            return;
        }
        Users.setPrefix(userId, prefix);
        e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.SUCCESS)
                .title("Prefix updated")
                .description("Your messages appear as:\n**[" + prefix + "] " + e.getUser().getName() + "** · Hello")
                .build())).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "`/prefix` set a call prefix (level " + Constants.PREFIX_MIN_LEVEL + "+)";
    }

    @Override
    public String getName() {
        return "prefix";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Set a call prefix (level " + Constants.PREFIX_MIN_LEVEL + "+)")
                .addOptions(new OptionData(OptionType.STRING, "prefix", "Prefix text")
                        .setRequired(false)
                        .setMaxLength(Constants.PREFIX_MAX_LENGTH))
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
