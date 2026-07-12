package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.service.CallResult;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/** Random chat between servers. */
public final class CallCommand implements ISlashCommand {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        CallResult result = calls.start(e.getChannel().getId(), e.getUser().getId());
        switch (result.status()) {
            case CONFLICT -> e.replyEmbeds(CallEmbeds.conflict()).setEphemeral(true).queue();
            case QUEUED -> e.replyEmbeds(CallEmbeds.queued(result.queuePosition(), result.queueSize())).queue();
            case ALREADY_QUEUED -> e.replyEmbeds(CallEmbeds.waiting(result.queuePosition(), result.queueSize())).queue();
            case MATCHED -> e.reply(calls.connectedMessage(result.session())).queue();
            case FAILED -> e.replyEmbeds(CallEmbeds.warn("Couldn't connect", result.message()))
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public String getHelp() {
        return "`/call` start a random chat with another server";
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
