package com.marsss.callerphone.channelpool.commands;

import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

public class PoolSettings implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        if (!ChannelPool.isHost(e.getChannel().getId())) {
            e.reply(PoolResponse.NOT_HOSTING.toString()).setEphemeral(true).queue();
            return;
        }

        TextInput publicity = TextInput.create("publicity", TextInputStyle.SHORT)
                .setPlaceholder("Set pool publicity here")
                .setMinLength(4)
                .setMaxLength(5)
                .setValue(ChannelPool.getPublicity(e.getChannel().getId()))
                .build();

        TextInput.Builder passwordBuilder = TextInput.create("password", TextInputStyle.SHORT)
                .setPlaceholder("Set pool password here")
                .setMinLength(5)
                .setMaxLength(30)
                .setRequired(false);

        String pwd = ChannelPool.getPassword(e.getChannel().getId());

        if (!pwd.isEmpty()) {
            passwordBuilder.setValue(pwd);
        }

        TextInput password = passwordBuilder.build();

        TextInput capacity = TextInput.create("capacity", TextInputStyle.SHORT)
                .setPlaceholder("Set pool capacity here")
                .setMinLength(1)
                .setMaxLength(2)
                .setValue(String.valueOf(ChannelPool.getCapacity(e.getChannel().getId())))
                .build();

        Modal modal = Modal.create("poolSettings", "Pool Channel Settings")
                .addComponents(Label.of("Pool Publicity (true | false)", publicity), Label.of("Pool Password", password), Label.of("Pool Capacity (1 - 10)", capacity))
                .build();


        e.replyModal(modal).queue();
    }

    @Override
    public String getHelp() {
        return "</poolsettings:1089656103391985664> - Configure channel pool settings.";
    }

    @Override
    public String getName() {
        return "poolsettings";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getHelp().split(" - ")[1])
                .setContexts(InteractionContextType.GUILD);
    }
}
