package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
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
        if (!ChannelPool.isHost(e.getChannel().getId())) {
            e.reply(PoolResponse.NOT_HOSTING.toString()).setEphemeral(true).queue();
            return;
        }

        StringSelectMenu publicity = StringSelectMenu.create("publicity")
                .setRequiredRange(1, 1)
                .addOption("true", "true")
                .addOption("false", "false")
                .setDefaultValues(String.valueOf(ChannelPool.getCapacity(e.getChannel().getId())))
                .build();

        TextInput.Builder passwordBuilder = TextInput.create("password", "Pool Password", TextInputStyle.SHORT)
                .setPlaceholder("Set pool password here")
                .setMinLength(5)
                .setMaxLength(30)
                .setRequired(false);

        String pwd = ChannelPool.getPassword(e.getChannel().getId());

        if (!pwd.isEmpty()) {
            passwordBuilder.setValue(pwd);
        }

        TextInput password = passwordBuilder.build();

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

        Modal modal = Modal.create("poolSettings", "Pool Channel Settings")
                .addComponents(ActionRow.of(publicity), ActionRow.of(password), ActionRow.of(capacity))
                .build();

        e.replyModal(modal).queue();
    }

    @Override
    public String getHelp() {
        return "</poolsettings:1089656103391985664> - Configure channel pool settings.";
    }

    @Override
    public String[] getTriggers() {
        return "poolsettings".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .setGuildOnly(true);
    }
}
