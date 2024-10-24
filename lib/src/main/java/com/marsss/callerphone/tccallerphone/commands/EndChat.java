package com.marsss.callerphone.tccallerphone.commands;

import com.marsss.commandType.ISlashCommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.tccallerphone.ChatResponse;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class EndChat implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.reply(endChat(e.getChannel())).queue();
    }

    private String endChat(MessageChannelUnion channel) {
        if(!TCCallerphone.hasCall(channel.getId())) {
            return ChatResponse.NO_CALL.toString();
        }
        return TCCallerphone.onEndCallCommand(channel);
    }

    @Override
    public String getHelp() {
        return "`/endchat` - End chatting with people from another server.";
    }

    @Override
    public String[] getTriggers() {
        return "end,hangup,endcall,endchat,enduserphone,endcallerphone,endphone,endphonecall".split(",");
    }
}
