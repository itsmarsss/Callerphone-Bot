package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ControlMessageStore;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.BrowseSession;
import com.itsmarsss.callerphone.match.service.DiscoveryService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

/**
 * Shared Discover card rendering and control-message tracking for edit-in-place.
 */
public final class DiscoveryUi {
    private DiscoveryUi() {
    }

    public static MessageCreateData cardMessage(
            MatchProfile subject,
            MatchProfile viewer,
            BrowseSession session,
            String remainingLine,
            String note
    ) {
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setEmbeds(MatchEmbeds.profileCard(subject, false, viewer))
                .setComponents(ActionRow.of(
                        Button.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, session.sessionId()),
                                "Interested"
                        ),
                        Button.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, session.sessionId()),
                                "Next"
                        ),
                        Button.danger(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_OPEN, "profile:" + session.subjectId()),
                                "Safety"
                        )
                ));
        String content = joinContent(note, remainingLine);
        if (content != null) {
            builder.setContent(content);
        }
        return builder.build();
    }

    public static MessageEditData cardEdit(
            MatchProfile subject,
            MatchProfile viewer,
            BrowseSession session,
            String remainingLine,
            String note
    ) {
        MessageEditBuilder builder = new MessageEditBuilder()
                .setEmbeds(MatchEmbeds.profileCard(subject, false, viewer))
                .setComponents(ActionRow.of(
                        Button.success(
                                MatchComponentIds.of(MatchComponentIds.ACTION_INTERESTED, session.sessionId()),
                                "Interested"
                        ),
                        Button.secondary(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SKIP, session.sessionId()),
                                "Next"
                        ),
                        Button.danger(
                                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_OPEN, "profile:" + session.subjectId()),
                                "Safety"
                        )
                ));
        builder.setContent(joinContent(note, remainingLine));
        return builder.build();
    }

    public static String remainingLine(ApplicationContext ctx, String userId) {
        return ctx.profiles().find(userId).map(p -> {
            ctx.profiles().resetDailyCountersIfNeeded(p);
            long left = Math.max(0, ctx.premium().dailyDiscoveries(userId) - p.getDiscoveryViewsToday());
            return left + " left today";
        }).orElse("");
    }

    public static void sendDiscover(
            InteractionHook hook,
            ApplicationContext ctx,
            String userId,
            boolean ephemeral
    ) {
        DiscoveryService.DiscoveryResult next = ctx.discovery().next(userId);
        if (!next.success()) {
            hook.sendMessage(ExperienceRenderer.toMessage(MatchPresenter.emptyDiscoverWithFallback(next.message())))
                    .setEphemeral(ephemeral)
                    .queue();
            return;
        }
        MatchProfile viewer = ctx.profiles().find(userId).orElse(null);
        String remaining = remainingLine(ctx, userId);
        hook.sendMessage(cardMessage(next.profile(), viewer, next.session(), remaining, null))
                .setEphemeral(ephemeral)
                .queue(msg -> ControlMessageStore.get().put(
                        ControlMessageStore.discoverKey(userId),
                        msg.getId()
                ));
    }

    public static void editDiscoverCard(
            InteractionHook hook,
            ApplicationContext ctx,
            String userId,
            String note
    ) {
        DiscoveryService.DiscoveryResult next = ctx.discovery().next(userId);
        if (!next.success()) {
            hook.editOriginal(ExperienceRenderer.toEdit(MatchPresenter.emptyDiscoverAfterAction(note, next.message())))
                    .queue();
            return;
        }
        MatchProfile viewer = ctx.profiles().find(userId).orElse(null);
        String remaining = remainingLine(ctx, userId);
        hook.editOriginal(cardEdit(next.profile(), viewer, next.session(), remaining, note)).queue();
    }

    private static String joinContent(String note, String remaining) {
        boolean hasNote = note != null && !note.isBlank();
        boolean hasRem = remaining != null && !remaining.isBlank();
        if (hasNote && hasRem) {
            return note + " · " + remaining;
        }
        if (hasNote) {
            return note;
        }
        if (hasRem) {
            return remaining;
        }
        return null;
    }
}
