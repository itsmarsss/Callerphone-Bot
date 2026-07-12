package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class ReportCallCommand implements ISlashCommand {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (calls.reportActive(e.getChannel().getId())) {
            e.replyEmbeds(CallEmbeds.success("Report received", "Recent messages were saved for review."))
                    .setEphemeral(true).queue();
        } else {
            e.replyEmbeds(CallEmbeds.noCall()).setEphemeral(true).queue();
        }
    }

    @Override
    public String getHelp() {
        return "`/reportcall` report the current call";
    }

    @Override
    public String getName() {
        return "reportcall";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Report the current call")
                .setContexts(InteractionContextType.GUILD);
    }
}
