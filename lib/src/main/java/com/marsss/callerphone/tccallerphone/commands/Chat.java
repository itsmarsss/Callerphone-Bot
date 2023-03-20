package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import com.marsss.callerphone.tccallerphone.ChatStatus;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
            case "familyfriendly":
                stat = chatFamilyFriendly(e.getChannel(), false);
                break;
            case "ffandanon":
                stat = chatFamilyFriendly(e.getChannel(), true);
                break;
        }

        if (stat == ChatStatus.CONFLICT) {
            e.reply(ChatResponse.ALREADY_CALL.toString()).setEphemeral(true).queue();
        } else if (stat == ChatStatus.NON_EXISTENT) {
            e.reply(ChatResponse.NO_PORT.toString()).setEphemeral(true).queue();
        } else if (stat == ChatStatus.SUCCESS_RECEIVER) {
            e.reply(ChatResponse.CALLING.toString()).queue();
            e.getChannel().sendMessage(ChatResponse.PICKED_UP.toString()).queue();
        } else if (stat == ChatStatus.SUCCESS_CALLER) {
            e.reply(ChatResponse.CALLING.toString()).queue();
        } else {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
        }
    }

    private ChatStatus chatUncensored(MessageChannelUnion channel, boolean anon) {
        if (TCCallerphone.hasCall(channel.getId())) {
            return ChatStatus.CONFLICT;
        }
        return TCCallerphone.onCallCommand(channel, false, anon);
    }

    private ChatStatus chatFamilyFriendly(MessageChannelUnion channel, boolean anon) {
        if (TCCallerphone.hasCall(channel.getId())) {
            return ChatStatus.CONFLICT;
        }
        return TCCallerphone.onCallCommand(channel, true, anon);
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "chat` - Chat with people from other servers.";
    }

    @Override
    public String[] getTriggers() {
        return "chat,call,callerphone,phone,userphone".split(",");
    }
}
