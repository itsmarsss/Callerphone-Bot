package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.discord.CallPresenter;
import com.itsmarsss.callerphone.call.model.CallSession;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.minigames.games.TicTacToe;
import com.itsmarsss.callerphone.users.BotUser;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;

/**
 * Plan philosophy §10.D — games live inside an active Call.
 * MVP: channel starters (or DM users) play Tic-Tac-Toe; call stays open.
 */
public final class CallGameService {
    private final CallSessionService sessions;

    public CallGameService(CallSessionService sessions) {
        this.sessions = sessions;
    }

    public Result proposeTtt(String sessionId, String proposerUserId, String fromChannelId) {
        Optional<CallSession> opt = sessions.getBySessionId(sessionId);
        if (opt.isEmpty() || !opt.get().ownsChannel(fromChannelId)) {
            return Result.fail("No active call.");
        }
        CallSession session = opt.get();
        MessageChannel other = ToolSet.getMessageChannel(session.otherChannelId(fromChannelId));
        if (other == null) {
            return Result.fail("Couldn't reach the other side.");
        }
        other.sendMessage(ExperienceRenderer.toMessage(
                CallPresenter.gameProposal(sessionId, proposerUserId)
        )).queue();
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(proposerUserId, "call_game_propose", sessionId);
            }
        } catch (Exception ignored) {
        }
        return Result.ok("Challenge sent. Waiting for them to accept.");
    }

    public Result acceptTtt(
            String sessionId,
            String acceptorUserId,
            String acceptorChannelId,
            User acceptorUser,
            User proposerUser
    ) {
        Optional<CallSession> opt = sessions.getBySessionId(sessionId);
        if (opt.isEmpty() || !opt.get().ownsChannel(acceptorChannelId)) {
            return Result.fail("That call is gone.");
        }
        if (proposerUser == null || acceptorUser == null) {
            return Result.fail("Couldn't resolve players.");
        }
        if (proposerUser.getId().equals(acceptorUserId)) {
            return Result.fail("Wait for the other side to accept.");
        }
        if (!Users.hasUser(proposerUser.getId()) || !Users.hasUser(acceptorUserId)) {
            return Result.fail("Both players need to accept Callerphone terms first (`/match join`).");
        }

        CallSession session = opt.get();
        String proposerChannel = session.otherChannelId(acceptorChannelId);
        TicTacToe ttt = new TicTacToe(proposerChannel, acceptorChannelId, proposerUser.getId(), acceptorUserId);

        BotUser fromUser = Users.getUser(proposerUser.getId());
        BotUser toUser = Users.getUser(acceptorUserId);
        if (fromUser == null || toUser == null || !fromUser.addGame(ttt) || !toUser.addGame(ttt)) {
            if (fromUser != null) {
                fromUser.removeGame(ttt.getID());
            }
            if (toUser != null) {
                toUser.removeGame(ttt.getID());
            }
            return Result.fail("One or both players are at the game limit.");
        }

        // Notify both call channels; full board still lives in DMs via existing mini-game flow
        MessageChannel a = ToolSet.getMessageChannel(session.getChannelA());
        MessageChannel b = ToolSet.getMessageChannel(session.getChannelB());
        MessageCreateData notice = new MessageCreateBuilder()
                .setContent("**Tic-Tac-Toe started.** The board is in each player's DMs. The call stays connected.")
                .build();
        if (a != null) {
            a.sendMessage(notice).queue();
        }
        if (b != null) {
            b.sendMessage(notice).queue();
        }

        ToolSet.sendPrivateGameMessageFrom(proposerUser,
                new MessageCreateBuilder()
                        .setContent("Your Tic-Tac-Toe with @" + acceptorUser.getName() + " (from a call).")
                        .build(),
                ttt);
        ToolSet.sendPrivateGameMessageTo(acceptorUser, ttt.getMessageForTo(), ttt);
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(acceptorUserId, "call_game_accept", sessionId);
            }
        } catch (Exception ignored) {
        }
        return Result.ok("Game started. Check your DMs for the board.");
    }

    public record Result(boolean success, String message) {
        public static Result ok(String m) {
            return new Result(true, m);
        }

        public static Result fail(String m) {
            return new Result(false, m);
        }
    }
}
