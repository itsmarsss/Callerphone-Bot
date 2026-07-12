package com.itsmarsss.callerphone.minigames.commands;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.call.service.CallGameService;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.minigames.games.TicTacToe;
import com.itsmarsss.callerphone.users.BotUser;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * Plan §24: explicit game shortcut. Infers Call/Connection parent when opponent is omitted.
 */
public class PlayMiniGame implements ISlashCommand {
    private final CallGameService callGames = new CallGameService(CallSessionService.get());

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (e.getSubcommandName() == null) {
            e.reply(Response.MISSING_PARAM.toString()).setEphemeral(true).queue();
            return;
        }
        if (!"tictactoe".equals(e.getSubcommandName())) {
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.WARNING)
                    .title("Not available")
                    .description("Only **Tic-Tac-Toe** is ready. Other games stay off the shelf until live.")
                    .build())).setEphemeral(true).queue();
            return;
        }

        OptionMapping opponentOpt = e.getOption("opponent");
        String userId = e.getUser().getId();
        String channelId = e.getChannel().getId();

        // Infer parent context when no opponent
        if (opponentOpt == null) {
            if (CallSessionService.get().isInCall(channelId)) {
                var session = CallSessionService.get().get(channelId).orElse(null);
                if (session != null) {
                    var result = callGames.proposeTtt(session.getId(), userId, channelId);
                    track(userId, "connection_game_start", "call_ttt");
                    e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(
                                    result.success()
                                            ? ExperienceIntent.SUCCESS
                                            : ExperienceIntent.WARNING)
                            .title(result.success() ? "Challenge sent" : "Couldn't challenge")
                            .description(result.message())
                            .build())).queue();
                    return;
                }
            }
            if (ApplicationContext.isReady()) {
                var ctx = ApplicationContext.get();
                String convId = ctx.enrollment().getOrCreate(userId).getSelectedConversationId();
                if (convId != null && !convId.isBlank()) {
                    var result = ctx.connectionGames().proposeTtt(convId, userId);
                    track(userId, "connection_game_start", "match_ttt");
                    e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(
                                    result.success()
                                            ? ExperienceIntent.SUCCESS
                                            : ExperienceIntent.WARNING)
                            .title(result.success() ? "Challenge sent" : "Couldn't challenge")
                            .description(result.message())
                            .build())).setEphemeral(true).queue();
                    return;
                }
            }
            e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.NEUTRAL)
                    .title("Pick a context")
                    .description(
                            "Challenge someone with `/game tictactoe @user`, or open an active **call** "
                                    + "or **Match chat** and run `/game tictactoe` without an opponent."
                    )
                    .build())).setEphemeral(true).queue();
            return;
        }

        User opponent = opponentOpt.getAsUser();
        if (!Users.hasUser(opponent.getId())) {
            e.reply("Your opponent has not yet agreed to our Privacy Policy or Terms of Service.")
                    .setEphemeral(true).queue();
            return;
        }
        if (e.getUser().getId().equals(opponent.getId())) {
            e.reply("You cannot challenge yourself to a MiniGame.").setEphemeral(true).queue();
            return;
        }
        if (opponent.isBot() || opponent.isSystem()) {
            e.reply("You cannot challenge this player.").setEphemeral(true).queue();
            return;
        }

        track(userId, "connection_game_start", "direct_ttt");
        e.reply(playTictactoe(e.getUser(), opponent)).queue();
    }

    private MessageCreateData playTictactoe(User from, User to) {
        MessageCreateBuilder message = new MessageCreateBuilder();
        TicTacToe ttt = new TicTacToe(null, null, from.getId(), to.getId());

        BotUser fromUser = Users.getUser(from.getId());
        BotUser toUser = Users.getUser(to.getId());

        if (fromUser == null || toUser == null
                || !fromUser.addGame(ttt)
                || !toUser.addGame(ttt)) {
            if (fromUser != null) {
                fromUser.removeGame(ttt.getID());
            }
            if (toUser != null) {
                toUser.removeGame(ttt.getID());
            }
            message.setContent("One or both players have reached the game limit...");
        } else {
            message.setContent("Successfully challenged " + to.getAsMention() + " to a game of TicTacToe.");
            ToolSet.sendPrivateGameMessageFrom(from,
                    new MessageCreateBuilder()
                            .setContent("Your game of TicTacToe with @" + to.getName() + " will show up here.")
                            .build(),
                    ttt);
            ToolSet.sendPrivateGameMessageTo(to, ttt.getMessageForTo(), ttt);
        }
        return message.build();
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
        return "`/game tictactoe [@opponent]` — during a call/Match chat, opponent is optional";
    }

    @Override
    public String getName() {
        return "game";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Play a minigame")
                .addSubcommands(
                        new SubcommandData("tictactoe", "Play Tic-Tac-Toe (opponent optional in call/Match chat)")
                                .addOptions(new OptionData(OptionType.USER, "opponent", "Who to challenge", false))
                )
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
