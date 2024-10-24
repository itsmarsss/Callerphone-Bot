package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import com.marsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
    public String[] getTriggers() {
        return "prefix,myprefix,setprefix".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .addOptions(
                        new OptionData(OptionType.STRING, "prefix", "Set prefix").setRequired(true)
                )
                .setGuildOnly(true);
    }
}
