package com.itsmarsss.callerphone.tccallerphone.commands;

import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.tccallerphone.ChatResponse;
import com.itsmarsss.callerphone.tccallerphone.ChatStatus;
import com.itsmarsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Chat implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        ChatStatus stat = null;
        switch (e.getSubcommandName()) {
            case "default":
                stat = chatUncensored(e.getChannel(), false);
                break;
            case "anonymous":
                stat = chatUncensored(e.getChannel(), true);
                break;
        }

        switch (stat) {
            case CONFLICT:
                e.reply(ChatResponse.ALREADY_CALL.toString()).setEphemeral(true).queue();
                return;
            case NON_EXISTENT:
                e.reply(ChatResponse.NO_PORT.toString()).setEphemeral(true).queue();
                return;
            case SUCCESS_RECEIVER:
                e.reply(ChatResponse.CALLING.toString()).queue();
                e.getChannel().sendMessage(ChatResponse.PICKED_UP.toString()).queue();
                return;
            case SUCCESS_CALLER:
                e.reply(ChatResponse.CALLING.toString()).queue();
                return;
            default:
                e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
        }
    }

    private ChatStatus chatUncensored(MessageChannelUnion channel, boolean anon) {
        if (TCCallerphone.hasCall(channel.getId())) {
            return ChatStatus.CONFLICT;
        }
        return TCCallerphone.onCallCommand(channel, anon);
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
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .addSubcommands(
                        new SubcommandData("default", "Chat with people from other servers"),
                        new SubcommandData("anonymous", "Chat with people from other servers anonymously."),
                        new SubcommandData("ffandanon", "Chat with people from other servers anonymously and with profanity blocked.")
                )
                .setContexts(InteractionContextType.GUILD);
    }
}
