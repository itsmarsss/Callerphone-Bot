package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Prefix implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final String[]ARGS = e.getMessage().getContentRaw().split("\\s+");

        if (ARGS.length == 1) {
            e.getMessage().reply(Response.MISSING_PARAM.toString()).queue();
            return;
        }

        final String PREFIX = ARGS[1];
        if (PREFIX.length() > 15) {
            e.getMessage().reply(CP_EMJ + "Prefix too long (Maximum length is 15 characters)").queue();
            return;
        }
        e.getMessage().reply(setPrefix(e.getAuthor(), PREFIX)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final String PREFIX = e.getOption("prefix").getAsString();
        if (PREFIX.length() > 15) {
            e.reply(CP_EMJ + "Prefix too long (Maximum length is 15 characters)").setEphemeral(true).queue();
            return;
        }
        e.reply(setPrefix(e.getUser(), PREFIX)).queue();
    }

    private final String CP_EMJ = Callerphone.Callerphone;
    private String setPrefix(User user, String prefix) {
        final long LVL = (Callerphone.getExecuted(user) + Callerphone.getTransmitted(user))/100;
        if(LVL < 50){
            return CP_EMJ + "You need to have at least 50 levels to set your own prefix.";
        }
        Callerphone.prefix.put(user.getId(), prefix);
        return CP_EMJ + "Successfully set your prefix to `" + prefix + "`!";
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
