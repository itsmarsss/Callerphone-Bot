package com.itsmarsss.callerphone.minigames.commands;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Plan §23: games shelf. Prefer launching from an active Call or Connection.
 * Only list ready games as playable.
 */
public class ShowMiniGames implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        boolean inCall = CallSessionService.get().isInCall(e.getChannel().getId());
        String body;
        if (inCall) {
            body = "**Play during this call**\n\n"
                    + "Tic-Tac-Toe · Ready — challenge a channel mate with `/game tictactoe @user`\n\n"
                    + "_Only ready games are listed. Connect Four and Battleship stay off this shelf until live._";
        } else {
            body = "Games work best **during a call** or inside a Match chat.\n\n"
                    + "**Ready**\n"
                    + "• Tic-Tac-Toe — `/game tictactoe @opponent`\n\n"
                    + "Start a `/call` or open a Match chat, then play together.";
        }
        e.reply(ExperienceRenderer.toMessage(ExperienceView.builder(ExperienceIntent.SOCIAL)
                .title(inCall ? "Play during this call" : "Mini games")
                .description(body)
                .build())).queue();
    }

    @Override
    public String getHelp() {
        return "`/games` list ready minigames";
    }

    @Override
    public String getName() {
        return "games";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "List ready minigames")
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
