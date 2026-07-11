package com.itsmarsss.callerphone.msginbottle.commands;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Cooldown;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class FindBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (e.getMember() == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }

        String userId = e.getMember().getId();
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(userId);
        if (elapsed < ToolSet.FINDBOTTLE_COOLDOWN) {
            long minutes = Math.max(1, (ToolSet.FINDBOTTLE_COOLDOWN - elapsed) / 60_000);
            e.reply(":warning: **Find MIB Cooldown;** " + minutes + " minute(s)")
                    .setEphemeral(true).queue();
            return;
        }

        Bottle bottle = MessageInBottle.findBottle();
        if (bottle == null) {
            e.reply(ToolSet.CP_EMJ + "No bottles floating right now. Try again later.")
                    .setEphemeral(true).queue();
            return;
        }

        MessageCreateData mibMessage = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (mibMessage == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }

        Cooldown.setMIBFindCoolDown(userId);
        e.reply(mibMessage).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "</findbottle:1089656103391985668> - Find a random message in bottle floating in the sea and read its message.";
    }

    @Override
    public String getName() {
        return "findbottle";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .setContexts(InteractionContextType.GUILD);
    }
}
