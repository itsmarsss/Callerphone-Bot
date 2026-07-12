package com.itsmarsss.callerphone.msginbottle.modals;

import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.match.service.SocialInboxService;
import com.itsmarsss.callerphone.msginbottle.MIBResponse;
import com.itsmarsss.callerphone.msginbottle.MIBStatus;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import com.itsmarsss.commandType.IModalInteraction;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class SendModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        String[] sendData = InteractionUtils.parseCustomId(e.getModalId());

        String id = sendData.length > 1 ? sendData[1] : null;

        String message = e.getValue("message").getAsString();
        String messageFiltered = ToolSet.filterMessage(message);

        if (!message.equals(messageFiltered)) {
            e.reply(MIBResponse.MESSAGE_FLAGGED.toString()).setEphemeral(true).queue();
            return;
        }

        Boolean signed = InteractionUtils.parseBooleanFromModal(e, "signed");

        if (signed == null) {
            e.reply(MIBResponse.INVALID_SIGNED.toString()).setEphemeral(true).queue();
            return;
        }

        MIBStatus stat = MessageInBottle.sendBottle(e.getUser().getId(), messageFiltered, signed, id);

        switch (stat) {
            case RATE_LIMITED:
                e.reply(MIBResponse.SEND_MAX.toString()).setEphemeral(true).queue();
                break;
            case ERROR:
                e.reply(Response.ERROR.toString()).setEphemeral(true).queue();
                break;
            case SENT:
                Cooldown.setMIBSendCoolDown(e.getUser().getId());
                if (id != null) {
                    notifyThreadParticipants(e.getUser().getId(), id, messageFiltered);
                }
                e.reply(MIBResponse.SEND_SUCCESS.toString()).setEphemeral(true).queue();
                break;
        }
    }

    private static void notifyThreadParticipants(String authorId, String bottleId, String preview) {
        if (!ApplicationContext.isReady()) {
            return;
        }
        try {
            Bottle bottle = MIB.getBottle(bottleId);
            if (bottle == null) {
                return;
            }
            String actorName = "Someone";
            if (ApplicationContext.get().profiles().find(authorId).isPresent()) {
                String n = ApplicationContext.get().profiles().find(authorId).get().getDisplayName();
                if (n != null && !n.isBlank()) {
                    actorName = n;
                }
            }
            String clip = preview == null ? "New reply" : preview.replace('\n', ' ').trim();
            if (clip.length() > 80) {
                clip = clip.substring(0, 79) + "…";
            }
            for (String participant : MIB.participantIds(bottle)) {
                if (participant.equals(authorId)) {
                    continue;
                }
                ApplicationContext.get().inbox().push(
                        participant,
                        SocialInboxService.EntryType.BOTTLE_REPLY,
                        bottleId,
                        actorName,
                        clip.isBlank() ? "New bottle reply" : clip
                );
            }
        } catch (Exception ignored) {
            // non-fatal
        }
    }

    @Override
    public String getID() {
        return "sendMIB";
    }
}
