package com.itsmarsss.callerphone.msginbottle.commands;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ViewBottle implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping idOpt = e.getOption("id");
        if (idOpt == null || idOpt.getAsString().trim().isEmpty()) {
            e.reply(ToolSet.CP_EMJ + "Provide a bottle ID, or use `/findbottle` for a random one.")
                    .setEphemeral(true).queue();
            return;
        }

        Bottle bottle = MIB.getBottle(idOpt.getAsString().trim());
        if (bottle == null) {
            e.reply(ToolSet.CP_EMJ + "No bottle found with that ID.").setEphemeral(true).queue();
            return;
        }

        MessageCreateData message = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (message == null) {
            e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
            return;
        }

        e.reply(message).setEphemeral(true).queue();
    }

    @Override
    public String getHelp() {
        return "</viewbottle> - View a message in bottle by ID.";
    }

    @Override
    public String getName() {
        return "viewbottle";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "View a message in bottle by ID")
                .addOptions(new OptionData(OptionType.STRING, "id", "Bottle ID").setRequired(true))
                .setContexts(InteractionContextType.GUILD);
    }
}
