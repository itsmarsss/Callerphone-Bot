package com.marsss.commandType;

import com.marsss.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ITextCommand extends ICommand {
    void runCommand(MessageReceivedEvent e);
}
