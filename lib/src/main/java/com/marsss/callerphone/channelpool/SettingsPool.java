package com.marsss.callerphone.channelpool;

import com.marsss.Command;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SettingsPool implements Command {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {

    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "Help Here";
    }

    @Override
    public String getHelpF() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "settingspool,settingpool,setpool".split(",");
    }
}