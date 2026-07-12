package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.commandType.IModalInteraction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.Arrays;
import java.util.List;

public final class MatchModalHandler implements IModalInteraction {
    @Override
    public void runModal(ModalInteractionEvent e) {
        if (!ApplicationContext.isReady()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.warn("One moment", "Still starting up.")))
                    .setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String modalId = e.getModalId();

        if (modalId.endsWith("setup")
                || modalId.endsWith("basics")
                || modalId.endsWith("bio")
                || modalId.endsWith("interests")) {
            handleSetup(e, ctx, userId);
            return;
        }
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.expired())).setEphemeral(true).queue();
    }

    private static void handleSetup(ModalInteractionEvent e, ApplicationContext ctx, String userId) {
        String name = value(e, "displayName");
        String bio = value(e, "bio");
        String prompt = value(e, "prompt");
        String interestsRaw = value(e, "interests");
        String pronouns = value(e, "pronouns");

        if (!name.isBlank() && bio.isBlank() && prompt.isBlank() && interestsRaw.isBlank()) {
            ctx.profiles().updateBasics(userId, name, null, pronouns, List.of());
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.incompleteWelcome()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        List<String> interests = Arrays.stream(interestsRaw.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        EnrollmentService.ServiceResult result = ctx.profiles().completeQuickSetup(
                userId,
                name,
                pronouns,
                bio,
                prompt,
                interests,
                e.getUser().getEffectiveAvatarUrl()
        );

        if (!result.success()) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.setupRetry(result.message())))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        try {
            ctx.analytics().track(userId, "match_setup_save", "ok");
        } catch (Exception ignored) {
        }

        MatchProfile profile = ctx.profiles().find(userId).orElse(null);
        if (profile != null && profile.getState() == ProfileState.ACTIVE) {
            e.reply(ExperienceRenderer.toMessage(MatchPresenter.previewLive(profile)))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        MatchProfile draft = ctx.profiles().find(userId).orElse(null);
        var user = ctx.enrollment().getOrCreate(userId);
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.incompleteWelcome(
                        com.itsmarsss.callerphone.match.service.ProfileChecklist.format(user, draft)
                )))
                .setEphemeral(true)
                .queue();
    }

    private static String value(ModalInteractionEvent e, String id) {
        return e.getValue(id) == null ? "" : e.getValue(id).getAsString();
    }

    @Override
    public String getID() {
        return "m";
    }
}
