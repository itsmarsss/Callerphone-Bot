package com.itsmarsss.callerphone.msginbottle.modals;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.service.SocialInboxService;
import com.itsmarsss.callerphone.msginbottle.BottlePresenter;
import com.itsmarsss.callerphone.msginbottle.MIBResponse;
import com.itsmarsss.callerphone.msginbottle.MIBStatus;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.callerphone.utils.InteractionUtils;
import com.itsmarsss.commandType.IModalInteraction;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

/**
 * Handles bottle compose modals.
 * <ul>
 *   <li>{@code sendMIB} — legacy: message + signed field</li>
 *   <li>{@code sendMIB-na} / {@code sendMIB-ns} — new bottle, identity pre-chosen</li>
 *   <li>{@code sendMIB-ra-{id}} / {@code sendMIB-rs-{id}} — reply, identity pre-chosen</li>
 *   <li>{@code sendMIB-{id}} — legacy reply with signed field</li>
 * </ul>
 */
public class SendModal implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        ParsedModal parsed = parseModalId(e.getModalId());
        String message = e.getValue("message").getAsString();
        String messageFiltered = ToolSet.filterMessage(message);

        if (!message.equals(messageFiltered)) {
            e.reply(MIBResponse.MESSAGE_FLAGGED.toString()).setEphemeral(true).queue();
            return;
        }

        boolean signed;
        if (parsed.signedPreset() != null) {
            signed = parsed.signedPreset();
        } else {
            Boolean fromField = InteractionUtils.parseBooleanFromModal(e, "signed");
            if (fromField == null) {
                e.reply(MIBResponse.INVALID_SIGNED.toString()).setEphemeral(true).queue();
                return;
            }
            signed = fromField;
        }

        // createMIB third arg is "signed" (historical naming in sendBottle used "anon" param name)
        MIBStatus stat = MessageInBottle.sendBottle(
                e.getUser().getId(),
                messageFiltered,
                signed,
                parsed.bottleId()
        );

        switch (stat) {
            case RATE_LIMITED -> e.reply(MIBResponse.SEND_MAX.toString()).setEphemeral(true).queue();
            case NOT_FOUND -> e.reply(ExperienceRenderer.toMessage(
                    com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                    com.itsmarsss.callerphone.experience.ExperienceIntent.WARNING)
                            .title("Not found")
                            .description("That bottle is gone.")
                            .build()
            )).setEphemeral(true).queue();
            case THREAD_FULL -> {
                String author = firstOtherSigned(parsed.bottleId(), e.getUser().getId());
                e.reply(ExperienceRenderer.toMessage(
                        BottlePresenter.threadFull(parsed.bottleId(), author)
                )).setEphemeral(true).queue();
            }
            case ERROR -> e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
            case SENT -> {
                Cooldown.setMIBSendCoolDown(e.getUser().getId());
                if (parsed.bottleId() != null) {
                    notifyThreadParticipants(e.getUser().getId(), parsed.bottleId(), messageFiltered);
                    track(e.getUser().getId(), "bottle_reply", parsed.bottleId());
                    e.reply(ExperienceRenderer.toMessage(BottlePresenter.replySent())).setEphemeral(true).queue();
                } else {
                    track(e.getUser().getId(), "bottle_send", signed ? "signed" : "anonymous");
                    e.reply(ExperienceRenderer.toMessage(BottlePresenter.sent())).setEphemeral(true).queue();
                }
            }
            default -> e.reply(ExperienceRenderer.toMessage(BottlePresenter.genericError())).setEphemeral(true).queue();
        }
    }

    /**
     * Historical quirk: {@link MessageInBottle#sendBottle} third parameter is passed to
     * createMIB as {@code signed}, despite the parameter name {@code anon} in older code.
     */
    private static ParsedModal parseModalId(String modalId) {
        if (modalId == null || modalId.isBlank() || "sendMIB".equals(modalId)) {
            return new ParsedModal(null, null);
        }
        // sendMIB-na / sendMIB-ns
        if ("sendMIB-na".equals(modalId)) {
            return new ParsedModal(null, false);
        }
        if ("sendMIB-ns".equals(modalId)) {
            return new ParsedModal(null, true);
        }
        // sendMIB-ra-{id} / sendMIB-rs-{id}
        if (modalId.startsWith("sendMIB-ra-") && modalId.length() > "sendMIB-ra-".length()) {
            return new ParsedModal(modalId.substring("sendMIB-ra-".length()), false);
        }
        if (modalId.startsWith("sendMIB-rs-") && modalId.length() > "sendMIB-rs-".length()) {
            return new ParsedModal(modalId.substring("sendMIB-rs-".length()), true);
        }
        // sendMIB-{bottleId} legacy
        if (modalId.startsWith("sendMIB-") && modalId.length() > "sendMIB-".length()) {
            return new ParsedModal(modalId.substring("sendMIB-".length()), null);
        }
        return new ParsedModal(null, null);
    }

    private static String firstOtherSigned(String bottleId, String viewerId) {
        if (bottleId == null) {
            return null;
        }
        Bottle bottle = MIB.getBottle(bottleId);
        if (bottle == null || bottle.getPages() == null) {
            return null;
        }
        for (var page : bottle.getPages()) {
            if (page.isSigned()
                    && page.getAuthor() != null
                    && !page.getAuthor().equals(viewerId)
                    && !"unknown".equals(page.getAuthor())) {
                return page.getAuthor();
            }
        }
        return null;
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
            var profile = ApplicationContext.get().profiles().find(authorId);
            if (profile.isPresent()) {
                String n = profile.get().getDisplayName();
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

    private static void track(String userId, String name, String meta) {
        try {
            if (ApplicationContext.isReady()) {
                ApplicationContext.get().analytics().track(userId, name, meta);
            }
        } catch (Exception ignored) {
        }
    }

    private record ParsedModal(String bottleId, Boolean signedPreset) {
    }

    @Override
    public String getID() {
        return "sendMIB";
    }
}
