package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.commandType.IButtonInteraction;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class BottleButtonHandler implements IButtonInteraction {
    @Override
    public void runClick(ButtonInteraction e) {
        BottleComponentIds.Parsed parsed = BottleComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            e.reply("That bottle button expired. Try `/bottle` again.").setEphemeral(true).queue();
            return;
        }
        String userId = e.getUser().getId();
        switch (parsed.action()) {
            case BottleComponentIds.ACTION_SEND -> {
                if (onSendCooldown(userId)) {
                    e.reply(ExperienceRenderer.toMessage(BottlePresenter.sendCooldown(sendMinutesLeft(userId))))
                            .setEphemeral(true).queue();
                    return;
                }
                e.reply(ExperienceRenderer.toMessage(BottlePresenter.identityPick())).setEphemeral(true).queue();
            }
            case BottleComponentIds.ACTION_ID_ANON -> {
                if (onSendCooldown(userId)) {
                    e.reply(ExperienceRenderer.toMessage(BottlePresenter.sendCooldown(sendMinutesLeft(userId))))
                            .setEphemeral(true).queue();
                    return;
                }
                e.replyModal(BottleModals.newAnonymous()).queue();
            }
            case BottleComponentIds.ACTION_ID_SIGN -> {
                if (onSendCooldown(userId)) {
                    e.reply(ExperienceRenderer.toMessage(BottlePresenter.sendCooldown(sendMinutesLeft(userId))))
                            .setEphemeral(true).queue();
                    return;
                }
                e.replyModal(BottleModals.newSigned()).queue();
            }
            case BottleComponentIds.ACTION_FIND -> find(e, userId);
            case BottleComponentIds.ACTION_REPLY_ANON -> {
                String bottleId = parsed.opaqueId();
                if (isThreadFull(bottleId)) {
                    e.reply(ExperienceRenderer.toMessage(threadFullView(bottleId, userId)))
                            .setEphemeral(true).queue();
                    return;
                }
                e.replyModal(BottleModals.replyAnonymous(bottleId)).queue();
            }
            case BottleComponentIds.ACTION_REPLY_SIGN -> {
                String bottleId = parsed.opaqueId();
                if (isThreadFull(bottleId)) {
                    e.reply(ExperienceRenderer.toMessage(threadFullView(bottleId, userId)))
                            .setEphemeral(true).queue();
                    return;
                }
                e.replyModal(BottleModals.replySigned(bottleId)).queue();
            }
            case BottleComponentIds.ACTION_KEEP_ANON -> e.reply(ExperienceRenderer.toMessage(
                    BottlePresenter.keepBrowsing()
            )).setEphemeral(true).queue();
            default -> e.reply("That bottle button expired. Try `/bottle` again.").setEphemeral(true).queue();
        }
    }

    private static void find(ButtonInteraction e, String userId) {
        if (onFindCooldown(userId)) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.findCooldown(findMinutesLeft(userId))))
                    .setEphemeral(true).queue();
            return;
        }
        Bottle bottle = MessageInBottle.findBottle();
        if (bottle == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.emptySea())).setEphemeral(true).queue();
            return;
        }
        MessageCreateData message = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (message == null) {
            e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            return;
        }
        Cooldown.setMIBFindCoolDown(userId);
        try {
            if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics()
                        .track(userId, "bottle_find", bottle.getId());
            }
        } catch (Exception ignored) {
        }
        e.reply(message).setEphemeral(true).queue();
    }

    private static boolean isThreadFull(String bottleId) {
        Bottle bottle = MIB.getBottle(bottleId);
        return bottle != null
                && bottle.getPages() != null
                && bottle.getPages().size() >= com.itsmarsss.callerphone.Constants.MIB_MAX_PAGES;
    }

    private static com.itsmarsss.callerphone.experience.ExperienceView threadFullView(
            String bottleId,
            String viewerId
    ) {
        Bottle bottle = MIB.getBottle(bottleId);
        String signedAuthor = null;
        if (bottle != null && bottle.getPages() != null) {
            for (var page : bottle.getPages()) {
                if (page.isSigned()
                        && page.getAuthor() != null
                        && !page.getAuthor().equals(viewerId)
                        && !"unknown".equals(page.getAuthor())) {
                    signedAuthor = page.getAuthor();
                    break;
                }
            }
        }
        return BottlePresenter.threadFull(bottleId, signedAuthor);
    }

    private static boolean onSendCooldown(String userId) {
        return System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(userId) < ToolSet.SENDBOTTLE_COOLDOWN;
    }

    private static boolean onFindCooldown(String userId) {
        return System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(userId) < ToolSet.FINDBOTTLE_COOLDOWN;
    }

    private static long sendMinutesLeft(String userId) {
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBSendCoolDown(userId);
        return Math.max(1, (ToolSet.SENDBOTTLE_COOLDOWN - elapsed) / 60_000);
    }

    private static long findMinutesLeft(String userId) {
        long elapsed = System.currentTimeMillis() - Cooldown.getMIBFindCoolDown(userId);
        return Math.max(1, (ToolSet.FINDBOTTLE_COOLDOWN - elapsed) / 60_000);
    }

    @Override
    public String getID() {
        return BottleComponentIds.PREFIX;
    }
}
