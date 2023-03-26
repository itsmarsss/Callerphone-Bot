package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.minigames.MiniGameStatus;
import com.marsss.callerphone.minigames.games.TicTacToe;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class OnButtonClick extends ListenerAdapter {
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

    }
}
