package com.marsss.callerphone.msginbottle.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.MIBStatus;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SendBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        User user = e.getUser();
        String message = e.getOption("message").getAsString();
        boolean anonymous = e.getOption("anonymous").getAsBoolean();

        MIBStatus stat = MessageInBottle.sendBottle(user.getId(), message, anonymous);

        switch(stat) {
            case RATE_LIMITED:
                e.reply(ToolSet.CP_ERR + "You have reached your max send limit.").setEphemeral(true).queue();
                break;
            case SENT:
                e.reply(ToolSet.CP_EMJ + "Your bottle has been sent!").setEphemeral(true).queue();
                break;
        }
    }


    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "sendbottle` - Send a message in bottle into the seas for someone to read.";
    }

    @Override
    public String[] getTriggers() {
        return "sendbottle,mibsend".split(",");
    }
}
