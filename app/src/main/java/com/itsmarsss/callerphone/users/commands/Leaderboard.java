package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Plan §22: activity leaders with medals and invoker context when available. */
public class Leaderboard implements ISlashCommand {
    private static final int TOP_N = 10;

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.deferReply().queue();
        List<Document> top = Users.topByCredits(TOP_N);
        if (top.isEmpty()) {
            e.getHook().editOriginal(ExperienceRenderer.toEdit(ExperienceView.builder(ExperienceIntent.SOCIAL)
                    .title("The board is open")
                    .description("Start a call or conversation to earn the first activity points.")
                    .actions(
                            com.itsmarsss.callerphone.experience.ActionSpec.secondary(
                                    com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                    "Start a call"
                            ),
                            com.itsmarsss.callerphone.experience.ActionSpec.success(
                                    com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                            com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE,
                                            "_"
                                    ),
                                    "Discover people"
                            )
                    )
                    .build())).queue();
            return;
        }

        AtomicInteger pending = new AtomicInteger(top.size());
        String[] lines = new String[top.size()];
        String invokerId = e.getUser().getId();
        int[] invokerRank = {-1};

        for (int i = 0; i < top.size(); i++) {
            final int index = i;
            Document doc = top.get(i);
            String userId = doc.getString("id");
            long credits = doc.get("credits") instanceof Number
                    ? ((Number) doc.get("credits")).longValue()
                    : 0L;
            if (invokerId.equals(userId)) {
                invokerRank[0] = index + 1;
            }
            String prefix = doc.getString("prefix");
            String prefixPart = (prefix != null && !prefix.isEmpty()) ? "*[" + prefix + "]* " : "";

            RestAction<User> action = ToolSet.getUser(userId);
            if (action == null) {
                lines[index] = formatLine(index + 1, prefixPart, userId, credits, invokerId.equals(userId));
                if (pending.decrementAndGet() == 0) {
                    sendBoard(e, lines, invokerRank[0]);
                }
                continue;
            }
            action.queue(
                    user -> {
                        lines[index] = formatLine(index + 1, prefixPart, user.getName(), credits, invokerId.equals(userId));
                        if (pending.decrementAndGet() == 0) {
                            sendBoard(e, lines, invokerRank[0]);
                        }
                    },
                    err -> {
                        lines[index] = formatLine(index + 1, prefixPart, userId, credits, invokerId.equals(userId));
                        if (pending.decrementAndGet() == 0) {
                            sendBoard(e, lines, invokerRank[0]);
                        }
                    }
            );
        }
    }

    private static String formatLine(int rank, String prefixPart, String name, long credits, boolean you) {
        String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "`#" + rank + "`";
        String mark = you ? "  ← You" : "";
        return medal + " " + prefixPart + "**" + name + "**  `" + credits + "`" + mark + "\n";
    }

    private static void sendBoard(SlashCommandInteractionEvent e, String[] lines, int invokerRank) {
        StringBuilder board = new StringBuilder();
        for (String line : lines) {
            if (line != null) {
                board.append(line);
            }
        }
        if (invokerRank > 0) {
            board.append("\nYour rank: **#").append(invokerRank).append("**");
        } else {
            board.append("\n_Not in the top ").append(lines.length).append(" yet — keep chatting and calling._");
        }
        e.getHook().editOriginal(ExperienceRenderer.toEdit(ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title("Activity leaders")
                .description(board.toString())
                .footer("Top " + lines.length + " by activity credits")
                .actions(
                        com.itsmarsss.callerphone.experience.ActionSpec.primary(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_REWARDS,
                                        "_"
                                ),
                                "View rewards"
                        ),
                        com.itsmarsss.callerphone.experience.ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        ),
                        com.itsmarsss.callerphone.experience.ActionSpec.success(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE,
                                        "_"
                                ),
                                "Discover people"
                        )
                )
                .build())).queue();
    }

    @Override
    public String getHelp() {
        return "`/leaderboard` top activity earners";
    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "View activity leaders")
                .setContexts(InteractionContextType.GUILD);
    }
}
