package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ChatFamilyFriendly implements ICommand {
    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        String[]args = e.getMessage().getContentRaw().split("\\s+");
        boolean anon = false;
        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("anon")) {
                anon = true;
            }
        }
        e.getMessage().reply(chat(e.getChannel(), anon)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {

    }

    private String chat(TextChannel channel, boolean anon) {
        if(TCCallerphone.hasCall(channel.getId())) {
            return Callerphone.Callerphone + "There is already a call going on!";
        }
        return TCCallerphone.onCallCommand(channel, true, anon);
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return "chatff,callff,callerphoneff,userphoneff,phoneff,phonecallff".split(",");
    }
}