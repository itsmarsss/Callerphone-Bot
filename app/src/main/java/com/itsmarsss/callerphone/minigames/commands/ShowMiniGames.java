package com.itsmarsss.callerphone.minigames.commands;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.call.discord.CallPresenter;
import com.itsmarsss.callerphone.call.model.CallSession;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.discord.match.MatchPresenter;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Plan §23: games shelf. Prefer launching from an active Call or Connection.
 * Only list ready games as playable — never a dead "coming soon" path.
 */
public class ShowMiniGames implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String channelId = e.getChannel().getId();
        String userId = e.getUser().getId();

        // 1) Active call in this channel
        if (CallSessionService.get().isInCall(channelId)) {
            CallSession session = CallSessionService.get().get(channelId).orElse(null);
            if (session != null) {
                track(userId, "games_shelf_open", "call:" + session.getId());
                e.reply(ExperienceRenderer.toMessage(CallPresenter.gameShelf(session.getId()))).queue();
                return;
            }
        }

        // 2) Selected Match connection
        if (ApplicationContext.isReady()) {
            var ctx = ApplicationContext.get();
            var user = ctx.enrollment().getOrCreate(userId);
            String convId = user.getSelectedConversationId();
            if (convId != null && !convId.isBlank()) {
                var conv = ctx.conversations().find(convId);
                if (conv.isPresent()
                        && conv.get().getParticipants() != null
                        && conv.get().getParticipants().contains(userId)) {
                    track(userId, "games_shelf_open", "connection:" + convId);
                    e.reply(ExperienceRenderer.toMessage(MatchPresenter.connectionGameShelf(convId)))
                            .setEphemeral(true)
                            .queue();
                    return;
                }
            }
        }

        // 3) No parent context
        track(userId, "games_shelf_open", "none");
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.generalGameShelf())).queue();
    }

    private static void track(String userId, String name, String meta) {
        try {
            if (ApplicationContext.isReady()) {
                ApplicationContext.get().analytics().track(userId, name, meta);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getHelp() {
        return "`/games` ready minigames (call or Match chat preferred)";
    }

    @Override
    public String getName() {
        return "games";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Play ready minigames during a call or Match chat")
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
