package com.itsmarsss.callerphone.msginbottle.handlers;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.msginbottle.BottlePresenter;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.IButtonInteraction;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

/**
 * Reply entry on a bottle. At the page cap, offer Match connection instead of another page.
 */
public class AddPageHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        String[] parts = e.getButton().getCustomId().split("-", 2);
        if (parts.length < 2) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        String bottleId = parts[1];
        Bottle bottle = MIB.getBottle(bottleId);
        if (bottle == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        if (bottle.getPages() != null && bottle.getPages().size() >= Constants.MIB_MAX_PAGES) {
            String signedAuthor = null;
            String viewerId = e.getUser().getId();
            for (var page : bottle.getPages()) {
                if (page.isSigned()
                        && page.getAuthor() != null
                        && !page.getAuthor().equals(viewerId)
                        && !"unknown".equals(page.getAuthor())) {
                    signedAuthor = page.getAuthor();
                    break;
                }
            }
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.threadFull(bottleId, signedAuthor)))
                    .setEphemeral(true).queue();
            return;
        }

        String authorId = e.getUser().getId();
        Boolean locked = com.itsmarsss.callerphone.msginbottle.MessageInBottle.identityLockForAuthor(bottle, authorId);
        if (locked != null) {
            // Already posted on this thread — skip identity picker
            e.replyModal(locked
                    ? com.itsmarsss.callerphone.msginbottle.BottleModals.replySigned(bottleId)
                    : com.itsmarsss.callerphone.msginbottle.BottleModals.replyAnonymous(bottleId)
            ).queue();
            return;
        }
        // First page on this bottle — identity picker (plan §25 / §10.E)
        e.reply(ExperienceRenderer.toMessage(BottlePresenter.replyIdentity(bottleId)))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public String getID() {
        return "adp";
    }
}
