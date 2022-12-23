package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.ChatStatus;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Chat implements ICommand {

    private final String CP_EMJ = Callerphone.Callerphone;
    private final String ALREADY_CALL = CP_EMJ + "There is already a call going on!";
    private final String NO_PORT = CP_EMJ + "Hmmm, I was unable to find an open port!";
    private final String CALLING = CP_EMJ + "Calling...";
    private final String PICKED_UP = CP_EMJ + "Someone picked up the phone!";
    private final String ERROR = CP_EMJ + "An error occurred.";

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final String MESSAGE = e.getMessage().getContentRaw();
        boolean anon = MESSAGE.contains("anon");
        boolean famfri = MESSAGE.contains("family");

        ChatStatus stat = (famfri ? chatFamilyFriendly(e.getChannel(), anon) : chatUncensor(e.getChannel(), anon));

        try {
            if (stat == ChatStatus.CONFLICT) {
                e.getMessage().reply(ALREADY_CALL).queue();
            } else if (stat == ChatStatus.NON_EXISTENT) {
                e.getMessage().reply(NO_PORT).queue();
            } else if (stat == ChatStatus.SUCCESS_RECEIVER) {
                e.getMessage().reply(CALLING).queue();
                e.getChannel().sendMessage(PICKED_UP).queue();
            } else if (stat == ChatStatus.SUCCESS_CALLER) {
                e.getMessage().reply(CALLING).queue();
            } else {
                e.getMessage().reply(ERROR).queue();
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        ChatStatus stat;
        switch (e.getSubcommandName()) {
            case "default":
                stat = chatUncensor(e.getTextChannel(), false);
                break;
            case "anonymous":
                stat = chatUncensor(e.getTextChannel(), true);
                break;
            case "familyfriendly":
                stat = chatFamilyFriendly(e.getTextChannel(), false);
                break;
            case "ffandanon":
                stat = chatFamilyFriendly(e.getTextChannel(), true);
                break;
            default:
                e.reply(Callerphone.Callerphone + "Hmmm, the slash command `" + e.getName() + " " + e.getSubcommandName() + "` shouldn't exist! Please join our support server and report this issue. " + Callerphone.support).queue();
                return;
        }
        try {
            if (stat == ChatStatus.CONFLICT) {
                e.reply(ALREADY_CALL).setEphemeral(true).queue();
            } else if (stat == ChatStatus.NON_EXISTENT) {
                e.reply(NO_PORT).setEphemeral(true).queue();
            } else if (stat == ChatStatus.SUCCESS_RECEIVER) {
                e.reply(CALLING).queue();
                e.getTextChannel().sendMessage(PICKED_UP).queue();
            } else if (stat == ChatStatus.SUCCESS_CALLER) {
                e.reply(CALLING).queue();
            } else {
                e.reply(ERROR).setEphemeral(true).queue();
            }
        } catch (Exception ex) {
        }
    }

    private ChatStatus chatUncensor(TextChannel channel, boolean anon) {
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
        return "`" + Callerphone.Prefix + "chat` - Chat with people from other servers.";
    }

    @Override
    public String[] getTriggers() {
        return "chat,call,callerphone,phone,userphone".split(",");
    }
}
