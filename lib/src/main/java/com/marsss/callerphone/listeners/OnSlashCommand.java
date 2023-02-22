package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class OnSlashCommand extends ListenerAdapter {

    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            if (Callerphone.cmdMap.containsKey(event.getName())) {

                if(!Storage.hasUser(event.getUser().getId())) {
                    ToolSet.sendPPAndTOS(event);
                    return;
                }

                if(Storage.isBlacklisted(event.getUser().getId())) {
                    event.reply(String.format(Response.BLACKLISTED.toString(), Storage.getReason(event.getUser().getId()))).setEphemeral(true).queue();
                    return;
                }

                Storage.reward(event.getUser(), 3);
                Storage.addExecute(event.getUser(), 1);

                Callerphone.cmdMap.get(event.getName()).runSlash(event);
                return;
            }
            event.reply(
                    ToolSet.CP_EMJ
                            + "Hmmm, the slash command `"
                            + event.getName()
                            + "` shouldn't exist! Please join our support server and report this issue. "
                            + Callerphone.config.getSupportServer()
            ).queue();
        } catch (Exception e) {
            e.printStackTrace();
            sendError(event, e);
        }
    }

    public static void sendError(SlashCommandEvent event, Exception error) {
        event.reply(String.format(Response.ERROR_MSG.toString(), error.toString())).queue();
    }
}
