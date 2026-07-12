package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.service.CallResult;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Random chat: guild channel-to-channel or user DM-to-DM. */
public final class CallCommand implements ISlashCommand {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String channelId = e.getChannel().getId();
        String userId = e.getUser().getId();
        boolean dm = isDm(e);
        CallEndpoint endpoint = dm
                ? CallEndpoint.dm(channelId, userId)
                : CallEndpoint.guild(channelId, userId);

        CallResult result = calls.start(endpoint);
        switch (result.status()) {
            case CONFLICT -> e.reply(ExperienceRenderer.toMessage(CallPresenter.conflict()))
                    .setEphemeral(true).queue();
            case QUEUED -> {
                ExperienceView view = dm
                        ? CallPresenter.queuedDm(result.queuePosition(), result.queueSize())
                        : CallPresenter.queued(result.queuePosition(), result.queueSize());
                e.reply(ExperienceRenderer.toMessage(view))
                        .queue(hook -> hook.retrieveOriginal().queue(msg ->
                                calls.rememberLobbyMessage(channelId, msg.getId())));
            }
            case ALREADY_QUEUED -> {
                ExperienceView view = dm
                        ? CallPresenter.waitingDm(result.queuePosition(), result.queueSize())
                        : CallPresenter.waiting(result.queuePosition(), result.queueSize());
                if (calls.tryEditLobby(channelId, view)) {
                    e.reply(ExperienceRenderer.toMessage(
                                    CallPresenter.success("Still waiting", "Queue position updated.")
                            ))
                            .setEphemeral(true)
                            .queue();
                } else {
                    e.reply(ExperienceRenderer.toMessage(view))
                            .queue(hook -> hook.retrieveOriginal().queue(msg ->
                                    calls.rememberLobbyMessage(channelId, msg.getId())));
                }
            }
            case MATCHED -> e.reply(calls.connectedMessage(result.session()))
                    .queue(hook -> hook.retrieveOriginal().queue(msg ->
                            calls.rememberLobbyMessage(channelId, msg.getId())));
            case FAILED -> e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.warn("Couldn't connect", result.message())))
                    .setEphemeral(true).queue();
        }
    }

    private static boolean isDm(SlashCommandInteractionEvent e) {
        ChannelType type = e.getChannel().getType();
        return type == ChannelType.PRIVATE || type == ChannelType.GROUP;
    }

    @Override
    public String getHelp() {
        return "`/call` start a random chat (server channel or DM)";
    }

    @Override
    public String getName() {
        return "call";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Start a random chat with another server or person")
                .setContexts(
                        InteractionContextType.GUILD,
                        InteractionContextType.BOT_DM,
                        InteractionContextType.PRIVATE_CHANNEL
                );
    }
}
