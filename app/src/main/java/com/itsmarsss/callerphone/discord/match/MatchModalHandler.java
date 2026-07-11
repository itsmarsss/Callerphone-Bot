package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.model.Gender;
import com.itsmarsss.commandType.IModalInteraction;
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

        if (modalId.endsWith("basics")) {
            String name = value(e, "displayName");
            Gender gender = Gender.fromCode(value(e, "gender")).orElse(null);
            String pronouns = value(e, "pronouns");
            List<Gender> openTo = MatchCommand.parseOpenTo(value(e, "openTo"));
            reply(e, ctx.profiles().updateBasics(userId, name, gender, pronouns, openTo));
            return;
        }
        if (modalId.endsWith("bio")) {
            reply(e, ctx.profiles().updateBioAndPrompt(userId, value(e, "bio"), value(e, "prompt")));
            return;
        }
        if (modalId.endsWith("interests")) {
            List<String> interests = Arrays.stream(value(e, "interests").split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            reply(e, ctx.profiles().updateInterests(userId, interests));
            return;
        }
        e.reply(ToolSet.CP_EMJ + " Unknown Match modal.").setEphemeral(true).queue();
    }

    private static String value(ModalInteractionEvent e, String id) {
        return e.getValue(id) == null ? "" : e.getValue(id).getAsString();
    }

    private static void reply(ModalInteractionEvent e, EnrollmentService.ServiceResult result) {
        e.reply(ToolSet.CP_EMJ + " " + result.message()).setEphemeral(true).queue();
    }

    @Override
    public String getID() {
        // Modal IDs are m:v1:modal:* — first segment before CUSTOM_ID_DELIMITER
        return "m";
    }
}
