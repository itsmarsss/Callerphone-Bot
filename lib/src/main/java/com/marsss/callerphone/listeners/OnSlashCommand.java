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

                if(Storage.isBlacklisted(event.getUser().getId())) {
                    event.reply(String.format(Response.BLACKLISTED.toString(), Storage.getReason(event.getUser().getId()))).setEphemeral(true).queue();
                    return;
                }

                Storage.reward(event.getUser(), 3);
                Storage.addExecute(event.getUser(), 1);

                if(Storage.getCredits(event.getUser()) == 0) {
                    event.replyEmbeds(
                            new EmbedBuilder()
                                    .setAuthor("Must Read", null, event.getUser().getAvatarUrl())
                                    .setTitle("User Agreement")
                                    .setDescription("By issuing another Callerphone (**\"Bot\"**) command, it is expected that you (**\"User\"**) have read, and User has agreed to both Bot's [Privacy Policy](" + Callerphone.config.getPrivacyPolicy() + ") and [Terms of Service](" + Callerphone.config.getTermsOfService() + "). It is User's responsibility to regularly check for updates to these documents.")
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
