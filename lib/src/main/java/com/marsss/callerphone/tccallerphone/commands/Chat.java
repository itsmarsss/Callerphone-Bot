package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import com.marsss.callerphone.tccallerphone.ChatStatus;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

public class Chat implements ICommand {

    @Override
    public void runCommand(MessageReceivedEvent e) {
        final String MESSAGE = e.getMessage().getContentRaw();
        boolean anon = MESSAGE.contains("anon");
        boolean famfri = MESSAGE.contains("family");

        ChatStatus stat = (famfri ? chatFamilyFriendly(e.getChannel(), anon) : chatUncensored(e.getChannel(), anon));

        if (stat == ChatStatus.CONFLICT) {
            e.getMessage().reply(ChatResponse.ALREADY_CALL.toString()).queue();
        } else if (stat == ChatStatus.NON_EXISTENT) {
            e.getMessage().reply(ChatResponse.NO_PORT.toString()).queue();
        } else if (stat == ChatStatus.SUCCESS_RECEIVER) {
            e.getMessage().reply(ChatResponse.CALLING.toString()).queue();
            e.getChannel().sendMessage(ChatResponse.PICKED_UP.toString()).queue();
        } else if (stat == ChatStatus.SUCCESS_CALLER) {
            e.getMessage().reply(ChatResponse.CALLING.toString()).queue();
        } else {
            e.getMessage().reply(Response.ERROR.toString()).queue();
        }
    }

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        ChatStatus stat = null;
        switch (e.getSubcommandName()) {
            case "default":
                stat = chatUncensored(e.getTextChannel(), false);
                break;
            case "anonymous":
                stat = chatUncensored(e.getTextChannel(), true);
                break;
            case "familyfriendly":
                stat = chatFamilyFriendly(e.getTextChannel(), false);
                break;
            case "ffandanon":
                stat = chatFamilyFriendly(e.getTextChannel(), true);
                break;
        }

        if (stat == ChatStatus.CONFLICT) {
            e.reply(ChatResponse.ALREADY_CALL.toString()).setEphemeral(true).queue();
        } else if (stat == ChatStatus.NON_EXISTENT) {
            e.reply(ChatResponse.NO_PORT.toString()).setEphemeral(true).queue();
        } else if (stat == ChatStatus.SUCCESS_RECEIVER) {
            e.reply(ChatResponse.CALLING.toString()).queue();
            e.getTextChannel().sendMessage(ChatResponse.PICKED_UP.toString()).queue();
        } else if (stat == ChatStatus.SUCCESS_CALLER) {
            e.reply(ChatResponse.CALLING.toString()).queue();
        } else {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
        }
    }

    private ChatStatus chatUncensored(TextChannel channel, boolean anon) {
        if (TCCallerphone.hasCall(channel.getId())) {
            return ChatStatus.CONFLICT;
        }
        return TCCallerphone.onCallCommand(channel, false, anon);
    }

    private ChatStatus chatFamilyFriendly(TextChannel channel, boolean anon) {
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
