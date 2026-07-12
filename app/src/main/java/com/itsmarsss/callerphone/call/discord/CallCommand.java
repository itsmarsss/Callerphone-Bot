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

/** Random chat: guild channel and bot DMs share one queue. */
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
        track(userId, result);
        switch (result.status()) {
            case CONFLICT -> e.reply(ExperienceRenderer.toMessage(CallPresenter.conflict())).queue();
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

    private static void track(String userId, CallResult result) {
        try {
            if (!com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                return;
            }
            String meta = switch (result.status()) {
                case QUEUED -> "queued";
                case ALREADY_QUEUED -> "already_queued";
                case MATCHED -> "matched";
                case CONFLICT -> "conflict";
                case FAILED -> "failed";
            };
            com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                    .track(userId, "call_start", meta);
        } catch (Exception ignored) {
        }
    }

    private static boolean isDm(SlashCommandInteractionEvent e) {
        ChannelType type = e.getChannel().getType();
        return type == ChannelType.PRIVATE || type == ChannelType.GROUP;
    }

    @Override
    public String getHelp() {
        return "`/call` start a random chat (server channel or DM — same queue)";
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
