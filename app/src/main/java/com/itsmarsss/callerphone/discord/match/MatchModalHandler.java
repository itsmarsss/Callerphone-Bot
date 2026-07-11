package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.identity.MatchUser;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.Gender;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.ProfileChecklist;
import com.itsmarsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.Arrays;
import java.util.List;

public final class MatchModalHandler implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        if (!ApplicationContext.isReady()) {
            e.reply(ToolSet.CP_EMJ + " Match is starting up.").setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String modalId = e.getModalId();

        EnrollmentService.ServiceResult result;
        if (modalId.endsWith("basics")) {
            String name = value(e, "displayName");
            Gender gender = Gender.fromCode(value(e, "gender")).orElse(null);
            String pronouns = value(e, "pronouns");
            List<Gender> openTo = MatchCommand.parseOpenTo(value(e, "openTo"));
            result = ctx.profiles().updateBasics(userId, name, gender, pronouns, openTo);
        } else if (modalId.endsWith("bio")) {
            result = ctx.profiles().updateBioAndPrompt(userId, value(e, "bio"), value(e, "prompt"));
        } else if (modalId.endsWith("interests")) {
            List<String> interests = Arrays.stream(value(e, "interests").split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            result = ctx.profiles().updateInterests(userId, interests);
        } else {
            e.reply(ToolSet.CP_EMJ + " Unknown Match modal.").setEphemeral(true).queue();
            return;
        }
        replyWithNext(e, ctx, userId, result);
    }

    private static String value(ModalInteractionEvent e, String id) {
        return e.getValue(id) == null ? "" : e.getValue(id).getAsString();
    }

    private static void replyWithNext(
            ModalInteractionEvent e,
            ApplicationContext ctx,
            String userId,
            EnrollmentService.ServiceResult result
    ) {
        MatchUser user = ctx.enrollment().getOrCreate(userId);
        MatchProfile profile = ctx.profiles().getOrCreateDraft(userId);
        var emb = result.success()
                ? MatchEmbeds.checklist(
                "Saved ✨",
                result.message(),
                ProfileChecklist.format(user, profile),
                ProfileChecklist.nextStep(user, profile))
                : MatchEmbeds.warm("Couldn't save that", result.message() + "\n\nTry again — no stress.");
        var reply = e.replyEmbeds(emb).setEphemeral(true);
        if (result.success() && ProfileChecklist.readyToSubmit(profile)
                && profile.getState() != com.itsmarsss.callerphone.match.model.ProfileState.ACTIVE) {
            reply = reply.addComponents(ActionRow.of(
                    Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_SUBMIT, "_"), "✦ Go live")
            ));
        }
        reply.queue();
    }

    @Override
    public String getID() {
        // Modal IDs are m:v1:modal:* — first segment before CUSTOM_ID_DELIMITER
        return "m";
    }
}
