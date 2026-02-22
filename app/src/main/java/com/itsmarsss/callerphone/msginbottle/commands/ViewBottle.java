package com.itsmarsss.callerphone.msginbottle.commands;

import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ViewBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

    }


    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public SlashCommandData getCommandData() {
        return null;
    }
}
