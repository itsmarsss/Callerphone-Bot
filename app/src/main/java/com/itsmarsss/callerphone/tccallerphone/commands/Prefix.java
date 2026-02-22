package com.itsmarsss.callerphone.tccallerphone.commands;

import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.callerphone.tccallerphone.ChatResponse;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Prefix implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        final String PREFIX = e.getOption("prefix").getAsString();

        e.reply(setPrefix(e.getUser(), PREFIX)).queue();
    }

    private String setPrefix(User user, String prefix) {

        if (prefix.length() > 15) {
            return ChatResponse.PREFIX_TOO_LONG.toString();
        }

        final long LVL = (Users.getExecuted(user.getId()) + Users.getTransmitted(user.getId())) / 100;
        if (LVL < 50) {
            return ChatResponse.LEVEL_LOW.toString();
        }
        Users.setPrefix(user.getId(), prefix);

        return String.format(ChatResponse.SET_PREFIX_SUCCESS.toString(), prefix);
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
                .addOptions(
                        new OptionData(OptionType.STRING, "prefix", "Set prefix").setRequired(true)
                )
                .setContexts(InteractionContextType.GUILD);
    }
}
