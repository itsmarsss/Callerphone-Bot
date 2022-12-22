package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnSlashCommand extends ListenerAdapter {

    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            if (Callerphone.cmdMap.containsKey(event.getName())) {
                Callerphone.cmdMap.get(event.getName()).runSlash(event);
                Callerphone.reward(event.getUser(), 3);
                Callerphone.addExecute(event.getUser(), 1);
                return;
            }
            event.reply(
                    Callerphone.Callerphone
                            + "Hmmm, the slash command `"
                            + event.getName()
                            + "` shouldn't exist! Please join our support server and report this issue. "
                            + Callerphone.support
            ).queue();
        } catch (Exception e) {
            e.printStackTrace();
            sendError(event, e);
        }
    }

    public static void sendError(SlashCommandEvent event, Exception error) {
        event.reply(String.format(Callerphone.ERROR_MSG, error.toString())).queue();
    }
}
