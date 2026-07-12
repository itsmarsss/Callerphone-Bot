package com.itsmarsss.callerphone.match.service;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ActionSpec;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.minigames.games.TicTacToe;
import com.itsmarsss.callerphone.users.BotUser;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plan philosophy §10.D — games bound to a Match connection (mediated chat).
 */
public final class ConnectionGameService {
    /** conversationId → proposer user id */
    private final ConcurrentHashMap<String, String> pending = new ConcurrentHashMap<>();

    public Optional<String> pendingProposer(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(pending.get(conversationId));
    }

    public Result proposeTtt(String conversationId, String proposerUserId) {
        if (!ApplicationContext.isReady()) {
            return Result.fail("Match is still starting up.");
        }
        ApplicationContext ctx = ApplicationContext.get();
        Optional<MatchConversation> opt = ctx.conversations().find(conversationId);
        if (opt.isEmpty() || !opt.get().getParticipants().contains(proposerUserId)) {
            return Result.fail("Chat not found.");
        }
        String otherId = opt.get().otherParticipant(proposerUserId);
        if (otherId == null) {
            return Result.fail("No other participant.");
        }
        String proposerName = ctx.profiles().find(proposerUserId)
                .map(MatchProfile::getDisplayName)
                .orElse("Someone");
        pending.put(conversationId, proposerUserId);
        try {
            ctx.analytics().track(proposerUserId, "connection_game_propose", conversationId);
        } catch (Exception ignored) {
        }
        ctx.inbox().push(
                otherId,
                SocialInboxService.EntryType.GAME_INVITE,
                conversationId,
                proposerName,
                "Challenged you to Tic-Tac-Toe"
        );
        var rest = ToolSet.getUser(otherId);
        if (rest != null) {
            rest.queue(user -> user.openPrivateChannel().queue(ch ->
                    ch.sendMessage(ExperienceRenderer.toMessage(
                            ExperienceView.builder(ExperienceIntent.SOCIAL)
                                    .title("Game challenge")
                                    .description("**" + proposerName
                                            + "** invited you to Tic-Tac-Toe in Match.")
                                    .actions(
                                            ActionSpec.success(
                                                    MatchComponentIds.of(
                                                            MatchComponentIds.ACTION_GAME_ACCEPT,
                                                            conversationId
                                                    ),
                                                    "Play"
                                            ),
                                            ActionSpec.secondary(
                                                    MatchComponentIds.of(
                                                            MatchComponentIds.ACTION_GAME_DECLINE,
                                                            conversationId
                                                    ),
                                                    "Not now"
                                            )
                                    )
                                    .build()
                    )).queue()
            ));
        }
        return Result.ok("Challenge sent. They'll see it in inbox and DMs.");
    }

    public Result acceptTtt(String conversationId, String acceptorUserId, User acceptor, User proposer) {
        if (!ApplicationContext.isReady()) {
            return Result.fail("Match is still starting up.");
        }
        ApplicationContext ctx = ApplicationContext.get();
        Optional<MatchConversation> opt = ctx.conversations().find(conversationId);
        if (opt.isEmpty() || !opt.get().getParticipants().contains(acceptorUserId)) {
            return Result.fail("Chat not found.");
        }
        String expected = pending.get(conversationId);
        if (expected == null) {
            return Result.fail("No pending challenge for this chat.");
        }
        if (proposer == null || acceptor == null) {
            return Result.fail("Couldn't resolve players.");
        }
        if (!expected.equals(proposer.getId())) {
            return Result.fail("That challenge isn't available.");
        }
        if (proposer.getId().equals(acceptorUserId)) {
            return Result.fail("Wait for them to accept.");
        }
        if (!opt.get().getParticipants().contains(proposer.getId())) {
            return Result.fail("Challenge expired.");
        }
        if (!Users.hasUser(proposer.getId()) || !Users.hasUser(acceptorUserId)) {
            return Result.fail("Both players need to accept Callerphone terms first.");
        }

        TicTacToe ttt = new TicTacToe(null, null, proposer.getId(), acceptorUserId);
        BotUser fromUser = Users.getUser(proposer.getId());
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

        pending.remove(conversationId);
        try {
            ctx.analytics().track(acceptorUserId, "connection_game_accept", conversationId);
        } catch (Exception ignored) {
        }

        ToolSet.sendPrivateGameMessageFrom(proposer,
                new MessageCreateBuilder()
                        .setContent("Your Tic-Tac-Toe with @" + acceptor.getName() + " (Match chat).")
                        .build(),
                ttt);
        ToolSet.sendPrivateGameMessageTo(acceptor, ttt.getMessageForTo(), ttt);

        ctx.inbox().push(proposer.getId(), SocialInboxService.EntryType.GAME_TURN,
                conversationId, acceptor.getName(), "Accepted Tic-Tac-Toe — your move is in DMs");
        return Result.ok("Game started. Check your DMs for the board. The Match chat stays open.");
    }

    public Result decline(String conversationId, String userId) {
        String expected = pending.get(conversationId);
        if (expected == null) {
            return Result.ok("No pending challenge.");
        }
        if (expected.equals(userId)) {
            pending.remove(conversationId);
            return Result.ok("Challenge cancelled.");
        }
        if (ApplicationContext.isReady()) {
            Optional<MatchConversation> opt = ApplicationContext.get().conversations().find(conversationId);
            if (opt.isPresent() && opt.get().getParticipants().contains(userId)) {
                pending.remove(conversationId);
                return Result.ok("Declined. You can keep chatting.");
            }
        }
        return Result.fail("No pending challenge for you.");
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
