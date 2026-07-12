package com.itsmarsss.callerphone.discord.match;

import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.DiscordLimits;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.component.MatchComponentIds;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.List;

/** Chat inbox layout: buttons for small lists, select menu when larger. */
public final class ChatInboxUi {
    public static final int SELECT_THRESHOLD = 4;

    private ChatInboxUi() {
    }

    public static void sendInbox(InteractionHook hook, ApplicationContext ctx, String userId) {
        List<MatchConversation> chats = ctx.conversations().list(userId);
        if (chats.isEmpty()) {
            hook.sendMessage(ExperienceRenderer.toMessage(MatchPresenter.chatsEmpty()))
                    .setEphemeral(true).queue();
            return;
        }

        StringBuilder sb = new StringBuilder();
        List<ChatRow> rows = new ArrayList<>();
        for (MatchConversation chat : chats) {
            String other = chat.otherParticipant(userId);
            String name = ctx.profiles().find(other).map(MatchProfile::getDisplayName).orElse("Connection");
            int unread = chat.unreadFor(userId);
            String badge = unread > 0 ? " · " + unread + " new" : "";
            String preview = chat.getLastMessagePreview() == null || chat.getLastMessagePreview().isBlank()
                    ? "No messages yet"
                    : truncate(chat.getLastMessagePreview(), 50);
            sb.append(unread > 0 ? "● " : "  ")
                    .append("**").append(name).append("**").append(badge)
                    .append("\n_").append(preview).append("_\n\n");
            rows.add(new ChatRow(chat.getConversationId(), name, unread, preview));
        }

        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setEmbeds(ExperienceRenderer.toEmbed(
                        com.itsmarsss.callerphone.experience.ExperienceView.builder(
                                        com.itsmarsss.callerphone.experience.ExperienceIntent.SOCIAL)
                                .title("Your chats")
                                .description(sb.toString().trim())
                                .build()
                ));

        if (rows.size() > SELECT_THRESHOLD) {
            StringSelectMenu.Builder menu = StringSelectMenu.create(
                            MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_MENU, "_"))
                    .setPlaceholder("Choose a chat")
                    .setRequiredRange(1, 1);
            int count = 0;
            for (ChatRow row : rows) {
                if (count >= DiscordLimits.SELECT_OPTIONS) {
                    break;
                }
                String label = truncate(row.name, 100);
                String desc = truncate((row.unread > 0 ? row.unread + " new · " : "") + row.preview, 100);
                menu.addOption(label, row.conversationId, desc);
                count++;
            }
            builder.setComponents(
                    ActionRow.of(menu.build()),
                    ActionRow.of(
                            Button.success(
                                    MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                                    "Discover more"
                            ),
                            Button.secondary(
                                    MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"),
                                    "Inbox"
                            ),
                            Button.secondary(
                                    com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                            com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                    ),
                                    "Find a bottle"
                            )
                    )
            );
        } else {
            List<Button> buttons = new ArrayList<>();
            List<ActionRow> actionRows = new ArrayList<>();
            for (ChatRow row : rows) {
                buttons.add(Button.primary(
                        MatchComponentIds.of(MatchComponentIds.ACTION_CHAT_SELECT, row.conversationId),
                        truncate(row.name, 20)
                ));
                if (buttons.size() == 5) {
                    actionRows.add(ActionRow.of(buttons));
                    buttons = new ArrayList<>();
                }
            }
            if (!buttons.isEmpty()) {
                actionRows.add(ActionRow.of(buttons));
            }
            actionRows.add(ActionRow.of(
                    Button.success(
                            MatchComponentIds.of(MatchComponentIds.ACTION_START_BROWSE, "_"),
                            "Discover more"
                    ),
                    Button.secondary(
                            MatchComponentIds.of(MatchComponentIds.ACTION_HOME, "_"),
                            "Inbox"
                    ),
                    Button.secondary(
                            com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                    com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                            ),
                            "Find a bottle"
                    )
            ));
            builder.setComponents(actionRows);
        }

        hook.sendMessage(builder.build()).setEphemeral(true).queue();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private record ChatRow(String conversationId, String name, int unread, String preview) {
    }
}
