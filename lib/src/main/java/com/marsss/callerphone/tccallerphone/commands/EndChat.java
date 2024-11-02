package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class EndChat implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(endChat(e.getChannel())).queue();
    }

    private MessageCreateData endChat(MessageChannelUnion channel) {
        if(!TCCallerphone.hasCall(channel.getId())) {
            return new MessageCreateBuilder().setContent(ChatResponse.NO_CALL.toString()).build();
        }

        return TCCallerphone.onEndCallCommand(channel);
    }

    @Override
    public String getHelp() {
        return "</endchat:1075168971977916467> - End chatting with people from another server.";
    }

    @Override
    public String[] getTriggers() {
        return "endchat,hangup,endcall,endchat,enduserphone,endcallerphone,endphone,endphonecall".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
