package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.call.service.CallResult;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Single-mode random chat between servers. */
public final class CallCommand implements ISlashCommand {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        CallResult result = calls.start(e.getChannel().getId(), e.getUser().getId());
        switch (result.status()) {
            case CONFLICT -> e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
            case ALREADY_QUEUED, QUEUED -> e.reply(ToolSet.CP_EMJ + " " + result.message()).queue();
            case MATCHED -> {
                e.reply(calls.connectedMessage(result.session())).queue();
            }
            case FAILED -> e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
        }
    }

    @Override
    public String getHelp() {
        return "`/call` — random chat with another server (single mode).\n"
                + "Share your Match profile during a call so they can like you.";
    }

    @Override
    public String getName() {
        return "call";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Start a random chat with another server")
                .setContexts(InteractionContextType.GUILD);
    }
}
