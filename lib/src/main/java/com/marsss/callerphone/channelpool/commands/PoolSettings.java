package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

public class PoolSettings implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {

        StringSelectMenu publicity = StringSelectMenu.create("publicity")
                .setRequiredRange(1, 1)
                .addOption("True", "true")
                .addOption("False", "false")
                .setDefaultValues(ChannelPool.getPublicity(e.getChannel().getId()))
                .build();

        TextInput password = TextInput.create("password", "Pool Password", TextInputStyle.SHORT)
                .setPlaceholder("Set pool password here")
                .setValue(ChannelPool.getPassword(e.getChannel().getId()))
                .build();

        StringSelectMenu capacity = StringSelectMenu.create("capacity")
                .setRequiredRange(1, 1)
                .addOption("1", "1")
                .addOption("2", "2")
                .addOption("3", "3")
                .addOption("4", "4")
                .addOption("5", "5")
                .addOption("6", "6")
                .addOption("7", "7")
                .addOption("8", "8")
                .addOption("9", "9")
                .addOption("10", "10")
                .setDefaultValues(String.valueOf(ChannelPool.getCapacity(e.getChannel().getId())))
                .build();

        Modal modal = Modal.create("poolConfig", "Pool Channel Settings")
                .addComponents(ActionRow.of(publicity), ActionRow.of(password), ActionRow.of(capacity))
                .build();

        e.replyModal(modal).queue();
    }

    private String poolProperties() {
        return "N/A";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String[] getTriggers() {
        return new String[0];
    }
}
