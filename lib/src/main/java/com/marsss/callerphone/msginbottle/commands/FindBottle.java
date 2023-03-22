package com.marsss.callerphone.msginbottle.commands;

import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FindBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

    }


    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "findbottle,mibfind".split(",");
    }
}
