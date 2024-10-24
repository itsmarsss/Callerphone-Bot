package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
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

        if (!ChannelPool.isHost(e.getChannel().getId())) {
            e.reply(PoolResponse.NOT_HOSTING.toString()).setEphemeral(true).queue();
            return;
        }

        TextInput publicity = TextInput.create("publicity", "Pool Publicity (True | False)", TextInputStyle.SHORT)
                .setPlaceholder("Set pool publicity here")
                .setMinLength(4)
                .setMaxLength(5)
                .setValue(ChannelPool.getPublicity(e.getChannel().getId()))
                .build();

        TextInput.Builder passwordBuilder = TextInput.create("password", "Pool Password", TextInputStyle.SHORT)
                .setPlaceholder("Set pool password here")
                .setMinLength(5)
                .setMaxLength(30)
                .setRequired(false);

        String pwd = ChannelPool.getPassword(e.getChannel().getId());

        if(!pwd.isEmpty()) {
            passwordBuilder.setValue(pwd);
        }

        TextInput password = passwordBuilder.build();

        TextInput capacity = TextInput.create("capacity", "Pool Capacity (1-10)", TextInputStyle.SHORT)
                .setPlaceholder("Set pool capacity here")
                .setMinLength(1)
                .setMaxLength(2)
                .setValue(String.valueOf(ChannelPool.getCapacity(e.getChannel().getId())))
                .build();

        Modal modal = Modal.create("poolSettings", "Pool Channel Settings")
                .addComponents(ActionRow.of(publicity), ActionRow.of(password), ActionRow.of(capacity))
                .build();

        e.replyModal(modal).queue();
    }

    @Override
    public String getHelp() {
        return "`/poolsettings` - Configure channel pool settings.";
    }

    @Override
    public String[] getTriggers() {
        return "poolsettings".split(",");
    }
}
