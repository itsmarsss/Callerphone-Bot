package com.itsmarsss.callerphone.tccallerphone.commands;

import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.tccallerphone.ChatResponse;
import com.itsmarsss.callerphone.tccallerphone.ChatResult;
import com.itsmarsss.callerphone.tccallerphone.entities.ChatMode;
import com.itsmarsss.callerphone.tccallerphone.services.ConversationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Chat implements ISlashCommand {
    private final ConversationService conversationService = ConversationService.getInstance();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        ChatMode mode = ChatMode.fromSubcommand(e.getSubcommandName());
        if (mode == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }

        ChatResult result = conversationService.startChat(e.getChannel().getId(), mode);

        switch (result.getStatus()) {
            case CONFLICT:
                e.reply(ChatResponse.ALREADY_CALL.toString()).setEphemeral(true).queue();
                break;
            case ALREADY_QUEUED:
                e.reply(ChatResponse.ALREADY_QUEUED.format(
                        result.getQueuePosition(),
                        result.getQueueSize()
                )).setEphemeral(true).queue();
                break;
            case NON_EXISTENT:
                e.reply(ChatResponse.NO_PORT.toString()).setEphemeral(true).queue();
                break;
            case SUCCESS_RECEIVER:
                e.reply(ChatResponse.CALLING.toString()).queue();
                e.getChannel().sendMessage(ChatResponse.PICKED_UP.toString()).queue();
                break;
            case SUCCESS_CALLER:
                e.reply(ChatResponse.QUEUED.format(
                        result.getQueuePosition(),
                        result.getQueueSize()
                )).queue();
                break;
            default:
                e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
        }
    }

    @Override
    public String getHelp() {
        return "</chat default:1075168968798634115> - Chat with people from other servers.\n" +
                "</chat anonymous:1075168968798634115> - Chat with people from other servers anonymously.\n" +
                "</chat ffandanon:1075168968798634115> - Chat with people from other servers anonymously and with profanity blocked.\n";
    }

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Chat with people from other servers")
                .addSubcommands(
                        new SubcommandData("default", "Chat with people from other servers"),
                        new SubcommandData("anonymous", "Chat with people from other servers anonymously."),
                        new SubcommandData("ffandanon", "Chat with people from other servers anonymously and with profanity blocked.")
                )
                .setContexts(InteractionContextType.GUILD);
    }
}
