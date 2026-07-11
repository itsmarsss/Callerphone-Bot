package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.ToolSet;
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
            e.reply(ToolSet.CP_EMJ + " Call marked for report. Transcript is saved when the call ends "
                    + "(or use the Report button after hang-up).").setEphemeral(true).queue();
        } else {
            e.reply(ToolSet.CP_EMJ + " No active call to report in this channel.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getHelp() {
        return "`/reportcall` — flag the current call for staff review.";
    }

    @Override
    public String getName() {
        return "reportcall";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Report the current random call")
                .setContexts(InteractionContextType.GUILD);
    }
}
