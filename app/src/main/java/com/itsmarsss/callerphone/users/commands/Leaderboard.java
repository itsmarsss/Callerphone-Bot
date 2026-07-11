package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.utils.EmbedHelpers;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Leaderboard implements ISlashCommand {
    private static final int TOP_N = 10;

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.deferReply().queue();

        List<Document> top = Users.topByCredits(TOP_N);
        if (top.isEmpty()) {
            e.getHook().editOriginalEmbeds(
                    EmbedHelpers.base()
                            .setTitle("Credits Leaderboard")
                            .setDescription("No credit data yet. Chat and use commands to climb the board!")
                            .build()
            ).queue();
            return;
        }

        AtomicInteger pending = new AtomicInteger(top.size());
        String[] lines = new String[top.size()];

        for (int i = 0; i < top.size(); i++) {
            final int index = i;
            Document doc = top.get(i);
            String userId = doc.getString("id");
            long credits = doc.get("credits") instanceof Number
                    ? ((Number) doc.get("credits")).longValue()
                    : 0L;
            String prefix = doc.getString("prefix");
            String prefixPart = (prefix != null && !prefix.isEmpty()) ? "*[" + prefix + "]* " : "";

            RestAction<User> action = ToolSet.getUser(userId);
            if (action == null) {
                lines[index] = formatLine(index + 1, prefixPart, userId, credits);
                if (pending.decrementAndGet() == 0) {
                    sendBoard(e, lines);
                }
                continue;
            }

            action.queue(
                    user -> {
                        lines[index] = formatLine(index + 1, prefixPart, user.getName(), credits);
                        if (pending.decrementAndGet() == 0) {
                            sendBoard(e, lines);
                        }
                    },
                    err -> {
                        lines[index] = formatLine(index + 1, prefixPart, userId, credits);
                        if (pending.decrementAndGet() == 0) {
                            sendBoard(e, lines);
                        }
                    }
            );
        }
    }

    private static String formatLine(int rank, String prefixPart, String name, long credits) {
        String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "`#" + rank + "`";
        return medal + " " + prefixPart + "**" + name + "** — `\u23E3 " + credits + "`\n";
    }

    private static void sendBoard(SlashCommandInteractionEvent e, String[] lines) {
        StringBuilder board = new StringBuilder();
        for (String line : lines) {
            if (line != null) {
                board.append(line);
            }
        }
        e.getHook().editOriginalEmbeds(new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("Credits Leaderboard")
                .setDescription(board.toString())
                .setFooter("Top " + lines.length + " by credits")
                .build()).queue();
    }

    @Override
    public String getHelp() {
        return "</leaderboard> - View the credits leaderboard.";
    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "View the credits leaderboard")
                .setContexts(InteractionContextType.GUILD);
    }
}
