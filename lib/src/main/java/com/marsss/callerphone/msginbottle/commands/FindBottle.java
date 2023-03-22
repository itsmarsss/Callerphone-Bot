package com.marsss.callerphone.msginbottle.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.msginbottle.MIBBottle;
import com.marsss.callerphone.msginbottle.MIBResponse;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class FindBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        MIBBottle bottle = findBottle(e.getUser().getId());

        EmbedBuilder lookingBottleEmbed = new EmbedBuilder()
                .setTitle("**Searching for a message bottle...**");

        EmbedBuilder bottleEmbed = new EmbedBuilder()
                .setTitle("**A message in bottle has arrived!**")
                .setDescription("**" + bottle.getParticipantID() + ":**")
                .appendDescription(bottle.getPage().get(0))
                .appendDescription("<t:" + bottle.getTimeSent() + ":R>")
                .setFooter("Picked Up:")
                .setTimestamp(Instant.now());

        e.replyEmbeds(lookingBottleEmbed.build()).queue((msg) -> {
            ((Message) msg).editMessageEmbeds(bottleEmbed.build()).queueAfter(1, TimeUnit.SECONDS);
        });
    }

    private MIBBottle findBottle(String id) {
        return MessageInBottle.findBottle(id);
    }


    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "findbottle` - Find a random bottle floating in the sea and read its message.";
    }

    @Override
    public String[] getTriggers() {
        return "findbottle,mibfind".split(",");
    }
}
