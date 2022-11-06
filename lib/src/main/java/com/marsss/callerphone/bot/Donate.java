package com.marsss.callerphone.bot;

import com.marsss.Command;
import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;

public class Donate implements Command {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        e.getMessage().replyEmbeds(new EmbedBuilder().
                setColor(Color.cyan)
                .setDescription("Donate at <" + Callerphone.donate + ">")
                .build()
        ).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "donate` - Help us out by donating.";
    }

    @Override
    public String[] getTriggers() {
        return "donate,don".split(",");
    }
}
