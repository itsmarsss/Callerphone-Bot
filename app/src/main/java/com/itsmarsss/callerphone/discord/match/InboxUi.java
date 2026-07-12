package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.DiscordLimits;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.service.MatchConversationService;
import com.itsmarsss.callerphone.match.service.SocialInboxService;
import com.itsmarsss.callerphone.msginbottle.MessageInBottle;
import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.database.categories.MIB;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;

/**
 * Plan philosophy §10.F — unified inbox list + open-entry routing.
 */
public final class InboxUi {
    private InboxUi() {
    }

    public static void send(InteractionHook hook, ApplicationContext ctx, String userId) {
        hook.sendMessage(buildList(ctx, userId)).setEphemeral(true).queue(msg ->
                com.itsmarsss.callerphone.experience.ControlMessageStore.get().put(
                        com.itsmarsss.callerphone.experience.ControlMessageStore.inboxKey(userId),
                        msg.getId()
                )
        );
    }

    public static MessageCreateData buildList(ApplicationContext ctx, String userId) {
        List<SocialInboxService.InboxEntry> entries = ctx.inbox().list(userId, 15);
        if (entries.isEmpty()) {
            return ExperienceRenderer.toMessage(MatchPresenter.inbox(List.of()));
        }

        StringBuilder sb = new StringBuilder();
        StringSelectMenu.Builder menu = StringSelectMenu.create(
                        MatchComponentIds.of(MatchComponentIds.ACTION_INBOX_MENU, "_"))
                .setPlaceholder("Choose an update")
                .setRequiredRange(1, 1);

        int i = 0;
        for (SocialInboxService.InboxEntry entry : entries) {
            String mark = entry.unread() ? "● " : "  ";
            String typeLabel = typeLabel(entry.type());
            sb.append(mark)
                    .append("**").append(entry.actorDisplay()).append("** · ").append(typeLabel)
                    .append("\n").append(entry.preview() == null ? "" : entry.preview())
                    .append("\n\n");
            if (i < DiscordLimits.SELECT_OPTIONS) {
                String label = truncate(entry.actorDisplay() + " · " + typeLabel, 100);
                String desc = truncate(entry.preview(), 100);
                menu.addOption(label, entry.id(), desc.isBlank() ? typeLabel : desc);
                i++;
            }
        }

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.success(
                MatchComponentIds.of(MatchComponentIds.ACTION_INBOX_OPEN, "_"),
                "Open next"
        ));
        buttons.add(Button.secondary(
                MatchComponentIds.of(MatchComponentIds.ACTION_INBOX_READ_ALL, "_"),
                "Mark all read"
        ));
        buttons.add(Button.primary(
                MatchComponentIds.of(MatchComponentIds.ACTION_OPEN_CHATS, "_"),
                "Open chats"
        ));

        return new MessageCreateBuilder()
                .setEmbeds(MatchEmbeds.soft("Your inbox", sb.toString().trim()))
                .setComponents(
                        ActionRow.of(menu.build()),
                        ActionRow.of(buttons)
                )
                .build();
    }

    public static MessageCreateData openEntry(
            ApplicationContext ctx,
            String userId,
            SocialInboxService.InboxEntry entry
    ) {
        if (entry == null) {
            return ExperienceRenderer.toMessage(MatchPresenter.warn(
                    "Nothing here",
                    "That update is gone. Open your inbox again."
            ));
        }
        ctx.inbox().markRead(userId, entry.id());
        return switch (entry.type()) {
            case CONNECTION_MESSAGE, GAME_INVITE, GAME_TURN -> openConversation(
                    ctx, userId, entry.sourceId(), entry
            );
            case BOTTLE_REPLY -> openBottle(ctx, userId, entry.sourceId());
            case INCOMING_INTEREST -> ExperienceRenderer.toMessage(MatchPresenter.incomingInterestFreeTeaser());
            case PROFILE_SHARE_RESPONSE -> openConversation(ctx, userId, entry.sourceId(), entry);
            case SAFETY_UPDATE -> ExperienceRenderer.toMessage(MatchPresenter.safetyHelp());
        };
    }

    public static MessageCreateData openNext(ApplicationContext ctx, String userId) {
        var next = ctx.inbox().firstUnread(userId);
        if (!next.isPresent()) {
            return ExperienceRenderer.toMessage(MatchPresenter.quietSuccess(
                    "All caught up",
                    "No unread updates. Discover someone new when you're ready."
            ));
        }
        return openEntry(ctx, userId, next.get());
    }

    private static MessageCreateData openConversation(
            ApplicationContext ctx,
            String userId,
            String conversationId,
            SocialInboxService.InboxEntry entry
    ) {
        if (conversationId == null || conversationId.isBlank()) {
            return fallbackEntry(entry);
        }
        MatchConversationService.SelectResult result = ctx.conversations().select(userId, conversationId);
        if (!result.success()) {
            // Source may still be a user id for interest, or stale chat
            return ExperienceRenderer.toMessage(MatchPresenter.warn(
                    entry.actorDisplay(),
                    entry.preview() + "\n\nOpen `/match chats` if this connection is still active."
            ));
        }
        MatchConversation conversation = result.conversation();
        String other = conversation.otherParticipant(userId);
        String name = ctx.profiles().find(other).map(MatchProfile::getDisplayName).orElse("your connection");
        List<Button> buttons = new ArrayList<>();
        List<Button> row1 = new ArrayList<>();
        List<Button> row2 = new ArrayList<>();
        row1.add(Button.primary(
                MatchComponentIds.of(MatchComponentIds.ACTION_GAME_TTT, conversationId),
                "Play a game"
        ));
        row1.add(Button.secondary(
                MatchComponentIds.of(MatchComponentIds.ACTION_ICEBREAKER, conversationId),
                "Icebreaker"
        ));
        row1.add(Button.secondary(
                MatchComponentIds.of(MatchComponentIds.ACTION_STOP_CHAT, "_"),
                "Stop chat"
        ));
        row2.add(Button.danger(
                MatchComponentIds.of(MatchComponentIds.ACTION_SAFETY_OPEN, "conversation:" + conversationId),
                "Safety"
        ));
        row2.add(Button.secondary(
                MatchComponentIds.of(MatchComponentIds.ACTION_BACK_INBOX, "_"),
                "Back to inbox"
        ));
        return new MessageCreateBuilder()
                .setEmbeds(MatchEmbeds.success(
                        "Chatting with " + name,
                        result.message() + "\n\n_" + entry.preview() + "_"
                ))
                .setComponents(ActionRow.of(row1), ActionRow.of(row2))
                .build();
    }

    private static MessageCreateData openBottle(ApplicationContext ctx, String userId, String bottleId) {
        Bottle bottle = MIB.getBottle(bottleId);
        if (bottle == null) {
            return ExperienceRenderer.toMessage(MatchPresenter.warn(
                    "Bottle gone",
                    "That bottle is no longer available."
            ));
        }
        ctx.inbox().markReadBySource(userId, bottleId);
        MessageCreateData bottleMsg = MessageInBottle.createMessage(bottle, Integer.MAX_VALUE);
        if (bottleMsg == null) {
            return ExperienceRenderer.toMessage(MatchPresenter.warn("Bottle", "Couldn't open that bottle."));
        }
        return bottleMsg;
    }

    private static MessageCreateData fallbackEntry(SocialInboxService.InboxEntry entry) {
        return ExperienceRenderer.toMessage(MatchPresenter.quietSuccess(
                entry.actorDisplay(),
                entry.preview() + "\n\nSource could not be opened."
        ));
    }

    private static String typeLabel(SocialInboxService.EntryType type) {
        return switch (type) {
            case CONNECTION_MESSAGE -> "message";
            case BOTTLE_REPLY -> "bottle reply";
            case GAME_INVITE -> "game invite";
            case GAME_TURN -> "your turn";
            case PROFILE_SHARE_RESPONSE -> "profile share";
            case SAFETY_UPDATE -> "safety";
            case INCOMING_INTEREST -> "interest";
        };
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
