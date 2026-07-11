package com.itsmarsss.callerphone.tccallerphone.commands;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.tccallerphone.ChatResponse;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Prefix implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping option = e.getOption("prefix");
        if (option == null) {
            e.reply(ChatResponse.PREFIX_TOO_LONG.toString()).setEphemeral(true).queue();
            return;
        }
        e.reply(setPrefix(e.getUser(), option.getAsString())).queue();
    }

    private String setPrefix(User user, String prefix) {
        if (prefix == null || prefix.isEmpty() || prefix.length() > Constants.PREFIX_MAX_LENGTH) {
            return ChatResponse.PREFIX_TOO_LONG.toString();
        }

        long level = (Users.getExecuted(user.getId()) + Users.getTransmitted(user.getId())) / 100;
        if (level < Constants.PREFIX_MIN_LEVEL) {
            return ChatResponse.LEVEL_LOW.toString();
        }

        Users.setPrefix(user.getId(), prefix);
        return ChatResponse.SET_PREFIX_SUCCESS.format(prefix);
    }

    @Override
    public String getHelp() {
        return "</prefix:1075168974934900806> - Set chat prefix (if you have more than 50 levels </profile:1075168888263815199>).";
    }

    @Override
    public String getName() {
        return "prefix";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .addOptions(new OptionData(OptionType.STRING, "prefix", "Set prefix")
                        .setRequired(true)
                        .setMaxLength(Constants.PREFIX_MAX_LENGTH))
                .setContexts(InteractionContextType.GUILD);
    }
}
