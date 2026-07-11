package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class EndCallCommand implements ISlashCommand {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(calls.end(e.getChannel().getId())).queue();
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
        return Commands.slash(getName(), "End a random call or leave the queue")
                .setContexts(InteractionContextType.GUILD);
    }
}
