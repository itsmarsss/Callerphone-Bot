package com.itsmarsss.callerphone.channelpool.modals;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.channelpool.ChannelPool;
import com.itsmarsss.callerphone.channelpool.PoolResponse;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import com.itsmarsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class SettingsModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        if (!ChannelPool.isHost(e.getChannel().getId())) {
            e.reply(PoolResponse.NOT_HOSTING.toString()).setEphemeral(true).queue();
            return;
        }

        Boolean publicity = InteractionUtils.parseBooleanFromModal(e, "publicity");

        if (publicity == null) {
            e.reply(PoolResponse.INVALID_PUBLICITY.toString()).setEphemeral(true).queue();
            return;
        }

        Integer capacity = InteractionUtils.parseIntegerFromModal(e, "capacity");

        if (capacity == null || capacity < Constants.POOL_MIN_CAPACITY || capacity > Constants.POOL_MAX_CAPACITY) {
            e.reply(PoolResponse.INVALID_CAPACITY.toString()).setEphemeral(true).queue();
            return;
        }

        String id = e.getChannel().getId();
        String password = e.getValue("password") != null ? e.getValue("password").getAsString() : "";
        if (password == null) {
            password = "";
        }

        ChannelPool.setPublicity(id, publicity);
        ChannelPool.setPassword(id, password);
        ChannelPool.setCap(id, capacity);

        String passwordDisplay = password.isEmpty() ? "`(none)`" : "||" + password + "||";
        e.reply(PoolResponse.SETTINGS_SUCCESS.format(publicity, passwordDisplay, capacity))
                .setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "poolSettings";
    }
}
