package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bot.Advertisement;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

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

                Random random = new Random();
                int randomNumber = random.nextInt(7) + 1;

                if(randomNumber == 1) {
                    event.getChannel().sendMessageEmbeds(Advertisement.generateAd()).queue();
                }
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
            ErrorHandler.handleSlashCommandError(event, e);
        }
    }
}
