package com.itsmarsss.commandType;

import com.itsmarsss.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ITextCommand extends ICommand {
    void runCommand(MessageReceivedEvent e);
}
