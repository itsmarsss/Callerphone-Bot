package com.marsss.callerphone.msginbottle.commands;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.entities.Bottle;
import com.marsss.callerphone.msginbottle.MIBResponse;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.ISlashCommand;
import com.marsss.database.categories.Cooldown;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.TimeUnit;

public class FindBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(e.getMember().getId()) < ToolSet.FINDBOTTLE_COOLDOWN) {
            e.reply(":warning: **Find MIB Cooldown;** " + ((ToolSet.FINDBOTTLE_COOLDOWN - (System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(e.getMember().getId()))) / 60000) + " minute(s)").setEphemeral(true).queue();
            return;
        }

        Bottle bottle = MessageInBottle.findBottle(e.getMember().getId());

        if (bottle == null) {
            e.reply(Response.ERROR.toString()).queue();
            return;
        }

        Cooldown.setMIBFindCoolDown(e.getMember().getId());
        e.reply(MessageInBottle.createMessage(bottle, Integer.MAX_VALUE)).setEphemeral(true).queueAfter(1, TimeUnit.SECONDS);
    }


    @Override
    public String getHelp() {
        return "</findbottle:1089656103391985668> - Find a random message in bottle floating in the sea and read its message.";
    }

    @Override
    public String[] getTriggers() {
        return "findbottle,mibfind".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
