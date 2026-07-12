package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.identity.EnrollmentService;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
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
            e.replyEmbeds(MatchEmbeds.warm("One moment", "Still starting up.")).setEphemeral(true).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        String modalId = e.getModalId();

        // One-shot setup (guided join) or legacy modal ids remapped to setup
        if (modalId.endsWith("setup")
                || modalId.endsWith("basics")
                || modalId.endsWith("bio")
                || modalId.endsWith("interests")) {
            handleSetup(e, ctx, userId);
            return;
        }
        e.replyEmbeds(MatchEmbeds.warm("That expired", "Open a fresh form to continue.")).setEphemeral(true).queue();
    }

    private static void handleSetup(ModalInteractionEvent e, ApplicationContext ctx, String userId) {
        // Prefer combined setup fields; fall back gracefully if old modal only sent some
        String name = value(e, "displayName");
        String bio = value(e, "bio");
        String prompt = value(e, "prompt");
        String interestsRaw = value(e, "interests");
        String pronouns = value(e, "pronouns");

        // Legacy partial modals: if only basics fields, keep old path
        if (!name.isBlank() && bio.isBlank() && prompt.isBlank() && interestsRaw.isBlank()) {
            ctx.profiles().updateBasics(userId, name, null, pronouns, List.of());
            e.replyEmbeds(MatchEmbeds.soft("Saved", "A bit more and you're done."))
                    .addComponents(ActionRow.of(
                            Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Continue")
                    ))
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
            e.replyEmbeds(MatchEmbeds.warm("Try again", result.message()))
                    .addComponents(ActionRow.of(
                            Button.primary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Retry")
                    ))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        MatchProfile profile = ctx.profiles().find(userId).orElse(null);
        var reply = e.replyEmbeds(
                MatchEmbeds.success("You're live", result.message()),
                profile != null ? MatchEmbeds.profileCard(profile, true) : MatchEmbeds.soft("Profile", "Saved.")
        ).setEphemeral(true);

        if (profile != null && profile.getState() == ProfileState.ACTIVE) {
            reply = reply.addComponents(ActionRow.of(
                    Button.success(MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"), "Discover"),
                    Button.secondary(MatchComponentIds.of(MatchComponentIds.ACTION_SETUP, "_"), "Edit")
            ));
        }
        reply.queue();
    }

    private static String value(ModalInteractionEvent e, String id) {
        return e.getValue(id) == null ? "" : e.getValue(id).getAsString();
    }

    @Override
    public String getID() {
        return "m";
    }
}
