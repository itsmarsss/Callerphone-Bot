package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class OnSlashCommand extends ListenerAdapter {

    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            if (Callerphone.cmdMap.containsKey(event.getName())) {

                Callerphone.reward(event.getUser(), 3);
                Callerphone.addExecute(event.getUser(), 1);

                if(Callerphone.getCredits(event.getUser()) == 0) {
                    event.replyEmbeds(
                            new EmbedBuilder()
                                    .setAuthor("Must Read", null, event.getUser().getAvatarUrl())
                                    .setTitle("User Agreement")
                                    .setDescription("By issuing another Callerphone (**\"Bot\"**) command, it is expected that you (**\"User\"**) have read, and User has agreed to both Bot's [Privacy Policy](" + Callerphone.privacy + ") and [Terms of Service](" + Callerphone.terms + "). It is User's responsibility to regularly check for updates to these documents.")
                                    .setFooter("This is to protect both Bot and User from unforeseen issues in the future. Please read these documents carefully.", Callerphone.jda.getSelfUser().getAvatarUrl())
                                    .setColor(new Color(114, 137, 218))
                                    .build()
                    ).setEphemeral(true).queue();
                    return;
                }

                Callerphone.cmdMap.get(event.getName()).runSlash(event);
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
