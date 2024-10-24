package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.commandType.ISlashCommand;
import com.marsss.database.categories.Cooldown;
import com.marsss.database.categories.Users;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnSlashCommand extends ListenerAdapter {

    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            if (Callerphone.cmdMap.containsKey(event.getName())) {

                if (!Users.hasUser(event.getUser().getId())) {
                    ToolSet.sendPPAndTOS(event);
                    return;
                }

                if (Users.isBlacklisted(event.getUser().getId())) {
                    event.reply(String.format(Response.BLACKLISTED.toString(), Users.getReason(event.getUser().getId()))).setEphemeral(true).queue();
                    return;
                }

                if (System.currentTimeMillis() - Cooldown.getCmdCooldown(event.getUser().getId()) < ToolSet.COMMAND_COOLDOWN) {
                    ToolSet.sendCommandCooldown(event);
                    return;
                }

                Cooldown.setCmdCooldown(event.getUser().getId());

                Users.reward(event.getUser().getId(), 3);
                Users.addExecute(event.getUser().getId(), 1);

                ((ISlashCommand) Callerphone.cmdMap.get(event.getName())).runSlash(event);
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

    public static void sendError(SlashCommandInteractionEvent event, Exception error) {
        event.reply(String.format(Response.ERROR_MSG.toString(), error.toString())).queue();
    }
}
