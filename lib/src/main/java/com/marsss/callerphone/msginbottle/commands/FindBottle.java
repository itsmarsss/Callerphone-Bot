package com.marsss.callerphone.msginbottle.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.msginbottle.MIBBottle;
import com.marsss.callerphone.msginbottle.MessageInBottle;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class FindBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        MIBBottle bottle = findBottle(e.getUser().getId());

        EmbedBuilder lookingBottleEmbed = new EmbedBuilder()
                .setTitle("**Searching for a message bottle...**")
                .setColor(new Color(114, 137, 218));

        User sender = ToolSet.getUser(bottle.getParticipantID().get(0));

        EmbedBuilder bottleEmbed = new EmbedBuilder()
                .setTitle("**A message in bottle has arrived!**")
                .setDescription("**" + sender.getName() + "**#" + sender.getDiscriminator() + ":\n\n")
                .appendDescription(bottle.getPage().get(0))
                .appendDescription("\n\n<t:" + bottle.getTimeSent() + ":R>")
                .setFooter("Time found")
                .setTimestamp(Instant.now())
                .setColor(new Color(114, 137, 218));

        Button reportButton = Button.danger("rpt-" + bottle.getUuid(), "Report");
        //Button saveUUID = Button.secondary("save-" + e.getUser().getId() + "-" + bottle.getUuid(), bottle.getUuid());

        MessageCreateBuilder message = new MessageCreateBuilder()
                .setEmbeds(bottleEmbed.build())
                .setComponents(ActionRow.of(reportButton/*, saveUUID*/));

        e.deferReply(true);

        e.reply(message.build()).setEphemeral(true).queueAfter(1, TimeUnit.SECONDS);
    }

    private MIBBottle findBottle(String id) {
        return MessageInBottle.findBottle(id);
    }


    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "findbottle` - Find a random message in bottle floating in the sea and read its message.";
    }

    @Override
    public String[] getTriggers() {
        return "findbottle,mibfind".split(",");
    }
}
