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
    private static final Random RANDOM = new Random();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            ISlashCommand command = (ISlashCommand) Callerphone.cmdMap.get(event.getName());
            if (command == null) {
                event.reply(com.itsmarsss.callerphone.experience.ExperienceRenderer.toMessage(
                        com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                        com.itsmarsss.callerphone.experience.ExperienceIntent.WARNING)
                                .title("Command unavailable")
                                .description("That command isn't registered right now. Try `/help` or join support: "
                                        + Callerphone.config.getSupportServer())
                                .build()
                )).setEphemeral(true).queue();
                return;
            }

            String userId = event.getUser().getId();
            if (!Users.hasUser(userId)) {
                ToolSet.sendPPAndTOS(event);
                return;
            }

            if (Users.isBlacklisted(userId)) {
                event.reply(Response.BLACKLISTED.format(
                        Users.getReason(userId),
                        Callerphone.config.getSupportServer()
                )).setEphemeral(true).queue();
                return;
            }

            if (System.currentTimeMillis() - Cooldown.getCmdCooldown(userId) < ToolSet.COMMAND_COOLDOWN) {
                ToolSet.sendCommandCooldown(event);
                return;
            }

            Cooldown.setCmdCooldown(userId);
            Users.reward(userId, 3);
            Users.addExecute(userId, 1);

            command.runSlash(event);

            if (RANDOM.nextInt(7) + 1 == 1) {
                event.getChannel().sendMessageEmbeds(Advertisement.generateAd()).queue();
            }
        } catch (Exception e) {
            ErrorHandler.handleSlashCommandError(event, e);
        }
    }
}
