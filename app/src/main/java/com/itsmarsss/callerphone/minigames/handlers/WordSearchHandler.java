package com.itsmarsss.callerphone.minigames.handlers;

import com.itsmarsss.callerphone.discord.match.MatchPresenter;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class WordSearchHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.generalGameShelf())).setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        return "wds";
    }
}
