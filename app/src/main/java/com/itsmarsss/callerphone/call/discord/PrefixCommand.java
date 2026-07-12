package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Level-gated call message prefix. */
public final class PrefixCommand implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping option = e.getOption("prefix");
        if (option == null) {
            e.reply("Choose a prefix up to " + Constants.PREFIX_MAX_LENGTH + " characters.")
                    .setEphemeral(true).queue();
            return;
        }
        e.reply(setPrefix(e.getUser(), option.getAsString())).queue();
    }

    private String setPrefix(User user, String prefix) {
        if (prefix == null || prefix.isEmpty() || prefix.length() > Constants.PREFIX_MAX_LENGTH) {
            return "Prefix is too long (max " + Constants.PREFIX_MAX_LENGTH + " characters).";
        }

        long level = (Users.getExecuted(user.getId()) + Users.getTransmitted(user.getId())) / 100;
        if (level < Constants.PREFIX_MIN_LEVEL) {
            return "Prefix unlocks at level " + Constants.PREFIX_MIN_LEVEL + ".";
        }

        Users.setPrefix(user.getId(), prefix);
        return "Prefix set to `" + prefix + "`.";
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
                        .setRequired(true)
                        .setMaxLength(Constants.PREFIX_MAX_LENGTH))
                .setContexts(InteractionContextType.GUILD);
    }
}
