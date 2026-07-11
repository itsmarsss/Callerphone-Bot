package com.itsmarsss.callerphone.tccallerphone.commands;

import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.callerphone.tccallerphone.ChatResponse;
import com.itsmarsss.callerphone.tccallerphone.services.ConversationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ReportChat implements ISlashCommand {
    private final ConversationService conversationService = ConversationService.getInstance();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (conversationService.reportActive(e.getChannel().getId())) {
            e.reply(ChatResponse.CHAT_REPORTED_SUCCESS.toString()).queue();
        } else {
            e.reply(ChatResponse.CHAT_REPORT_VIA_SERVER.toString()).queue();
        }
    }

    @Override
    public String getHelp() {
        return "</reportchat:1075168978189692948> - Report current chat with another server.";
    }

    @Override
    public String getName() {
        return "reportchat";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .setContexts(InteractionContextType.GUILD);
    }
}
