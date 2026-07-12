package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Plan §18: confirm before ending an active call; leave queue / idle without extra friction.
 */
public final class EndCallCommand implements ISlashCommand {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String channelId = e.getChannel().getId();
        if (calls.isInCall(channelId)) {
            e.reply(ExperienceRenderer.toMessage(CallPresenter.endConfirm())).queue();
            return;
        }
        // Queued or idle — end immediately
        CallSessionService.EndOutcome outcome = calls.end(channelId);
        if (outcome.editedInPlace()) {
            e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.success("Done", "Lobby updated.")
                    ))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        e.reply(outcome.message()).queue();
    }

    @Override
    public String getHelp() {
        return "`/endcall` hang up or leave the queue";
    }

    @Override
    public String getName() {
        return "endcall";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "End a call or leave the queue")
                .setContexts(
                        InteractionContextType.GUILD,
                        InteractionContextType.BOT_DM,
                        InteractionContextType.PRIVATE_CHANNEL
                );
    }
}
