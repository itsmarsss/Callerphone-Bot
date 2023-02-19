package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Prefix implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final String[] ARGS = e.getMessage().getContentRaw().split("\\s+");

        if (ARGS.length == 1) {
            e.getMessage().reply(Response.MISSING_PARAM.toString()).queue();
            return;
        }

        final String PREFIX = ARGS[1];

        e.getMessage().reply(setPrefix(e.getAuthor(), PREFIX)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final String PREFIX = e.getOption("prefix").getAsString();

        e.reply(setPrefix(e.getUser(), PREFIX)).queue();
    }

    private String setPrefix(User user, String prefix) {

        if (prefix.length() > 15) {
            return ChatResponse.PREFIX_TOO_LONG.toString();
        }

        final long LVL = (Callerphone.getExecuted(user) + Callerphone.getTransmitted(user)) / 100;
        if (LVL < 50) {
            return ChatResponse.LEVEL_LOW.toString();
        }
        Callerphone.prefix.put(user.getId(), prefix);

        return String.format(ChatResponse.SET_PREFIX_SUCCESS.toString(), prefix);
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "prefix <prefix>` - Set chat prefix (if more than lvl 50).";
    }

    @Override
    public String[] getTriggers() {
        return "prefix,myprefix,setprefix".split(",");
    }
}
