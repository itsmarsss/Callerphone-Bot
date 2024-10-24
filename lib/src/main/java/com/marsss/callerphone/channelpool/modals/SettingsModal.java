package com.marsss.callerphone.channelpool.modals;

import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolResponse;
import com.marsss.callerphone.channelpool.PoolStatus;
import com.marsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.nio.channels.Channel;

public class SettingsModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        if (!ChannelPool.isHost(e.getChannel().getId())) {
            e.reply(PoolResponse.NOT_HOSTING.toString()).setEphemeral(true).queue();
            return;
        }

        String publicityStr = e.getValue("publicity").getAsString().toLowerCase();
        boolean publicity;

        if (publicityStr.equals("true")) {
            publicity = true;
        } else if (publicityStr.equals("false")) {
            publicity = false;
        } else {
            e.reply(PoolResponse.INVALID_PUBLICITY.toString()).setEphemeral(true).queue();
            return;
        }

        String capacityStr = e.getValue("capacity").getAsString().toLowerCase();
        int capacity;

        try {
            capacity = Integer.parseInt(capacityStr);

            if (capacity < 2 || capacity > 10) {
                e.reply(PoolResponse.INVALID_CAPACITY.toString()).setEphemeral(true).queue();
                return;
            }
        } catch (Exception ex) {
            e.reply(PoolResponse.INVALID_CAPACITY.toString()).setEphemeral(true).queue();
            return;
        }

        String id = e.getChannel().getId();

        String password = e.getValue("password").getAsString();

        ChannelPool.setPublicity(id, publicity);
        ChannelPool.setPassword(id, password);
        ChannelPool.setCap(id, capacity);

        e.reply(String.format(PoolResponse.SETTINGS_SUCCESS.toString(), publicity, "||" + password + "||", capacity)).setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "poolSettings";
    }
}
