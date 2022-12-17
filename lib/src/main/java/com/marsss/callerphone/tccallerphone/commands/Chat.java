package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.ChatStatus;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Chat implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        String msg  = e.getMessage().getContentRaw();
        boolean anon = msg.contains("anon");
        boolean famfri = msg.contains("family");

        ChatStatus stat = (famfri == true ? chatFamilyFriendly(e.getChannel(), anon) : chatUncensor(e.getChannel(), anon));

        if(stat == ChatStatus.CONFLICT) {
            e.getMessage().reply(Callerphone.Callerphone + "There is already a call going on!").queue();
        }else if (stat == ChatStatus.NON_EXISTENT) {
            e.getMessage().reply(Callerphone.Callerphone + "Hmmm, I was unable to find an open port!").queue();
        } else if (stat == ChatStatus.SUCCESS_RECEIVER) {
            e.getMessage().reply(Callerphone.Callerphone + "Calling...").queue();
            e.getChannel().sendMessage(Callerphone.Callerphone + "Someone picked up the phone!").queue();
        } else if (stat == ChatStatus.SUCCESS_CALLER) {
            e.getMessage().reply(Callerphone.Callerphone + "Calling...").queue();
        }
        e.getMessage().reply(Callerphone.Callerphone + "An error occurred.").queue();
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
        if(stat == ChatStatus.CONFLICT) {
            e.reply(Callerphone.Callerphone + "There is already a call going on!").setEphemeral(true).queue();
        }else if (stat == ChatStatus.NON_EXISTENT) {
            e.reply(Callerphone.Callerphone + "Hmmm, I was unable to find an open port!").setEphemeral(true).queue();
        } else if (stat == ChatStatus.SUCCESS_RECEIVER) {
            e.reply(Callerphone.Callerphone + "Calling...").queue();
            e.getTextChannel().sendMessage(Callerphone.Callerphone + "Someone picked up the phone!").queue();
        } else if (stat == ChatStatus.SUCCESS_CALLER) {
            e.reply(Callerphone.Callerphone + "Calling...").queue();
        }
        e.reply(Callerphone.Callerphone + "An error occurred.").setEphemeral(true).queue();
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
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "chat,call,callerphone,phone,userphone".split(",");
    }
}
