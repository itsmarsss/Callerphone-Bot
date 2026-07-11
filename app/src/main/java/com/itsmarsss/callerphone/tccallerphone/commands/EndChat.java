package com.itsmarsss.callerphone.tccallerphone.commands;

import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.callerphone.tccallerphone.services.ConversationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class EndChat implements ISlashCommand {
    private final ConversationService conversationService = ConversationService.getInstance();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(conversationService.endConversation(e.getChannel().getId())).queue();
    }

    @Override
    public String getHelp() {
        return "</endchat:1075168971977916467> - End chatting with people from another server.";
    }

    @Override
    public String getName() {
        return "endchat";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .setContexts(InteractionContextType.GUILD);
    }
}
