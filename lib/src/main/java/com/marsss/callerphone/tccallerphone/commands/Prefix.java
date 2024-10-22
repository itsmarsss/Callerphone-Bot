package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.database.Storage;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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

        final long LVL = (Storage.getExecuted(user.getId()) + Storage.getTransmitted(user.getId())) / 100;
        if (LVL < 50) {
            return ChatResponse.LEVEL_LOW.toString();
        }
        Storage.setPrefix(user.getId(), prefix);

        return String.format(ChatResponse.SET_PREFIX_SUCCESS.toString(), prefix);
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "prefix <prefix>` - Set chat prefix (if more than lvl 50).";
    }

    @Override
    public String[] getTriggers() {
        return "prefix,myprefix,setprefix".split(",");
    }
}
