package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.model.CallEndpoint;
import com.itsmarsss.callerphone.call.service.CallGameService;
import com.itsmarsss.callerphone.call.service.CallProfileShareService;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.safety.ReportCategory;
import com.itsmarsss.commandType.IButtonInteraction;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public final class CallButtonHandler implements IButtonInteraction {
    private final CallSessionService sessions = CallSessionService.get();
    private final CallProfileShareService share = new CallProfileShareService(sessions);
    private final CallGameService games = new CallGameService(sessions);

    @Override
    public void runClick(ButtonInteraction e) {
        CallComponentIds.Parsed parsed = CallComponentIds.parse(e.getComponentId());
        if (parsed == null) {
            if (e.getComponentId().startsWith("reportchat-")) {
                String id = e.getComponentId().substring("reportchat-".length());
                sessions.reportById(id);
                e.editButton(Button.danger("reportchat", "Reported").asDisabled()).queue();
                e.getMessage().reply(ExperienceRenderer.toMessage(
                        CallPresenter.success("Report received", "Thanks for reporting.")
                )).queue();
                return;
            }
            e.reply(ExperienceRenderer.toMessage(
                    CallPresenter.warnRecover("That expired", "Open a fresh screen to continue.")
            )).setEphemeral(true).queue();
            return;
        }
        wireShare();
        String userId = e.getUser().getId();
        String channelId = e.getChannel().getId();
        switch (parsed.action()) {
            case CallComponentIds.SHARE -> {
                var result = share.share(parsed.sessionId(), userId, channelId);
                e.reply(ExperienceRenderer.toMessage(result.success()
                                ? CallPresenter.successInCall("Profile shared", result.message(), parsed.sessionId())
                                : callShareFail(result.message())
                        ))
                        .setEphemeral(true).queue();
            }
            case CallComponentIds.LIKE -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), true, channelId);
                e.reply(ExperienceRenderer.toMessage(result.success()
                                ? CallPresenter.successInCall("Interest sent", result.message(), parsed.sessionId())
                                : callShareFail(result.message())
                        ))
                        .setEphemeral(true).queue();
            }
            case CallComponentIds.PASS -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), false, channelId);
                e.reply(ExperienceRenderer.toMessage(
                        CallPresenter.successInCall("Noted", result.message(), parsed.sessionId())
                )).setEphemeral(true).queue();
            }
            case CallComponentIds.REPORT -> openReportCategories(e, parsed.sessionId());
            case CallComponentIds.LEAVE_QUEUE -> {
                try {
                    if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                        var analytics = com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics();
                        analytics.track(userId, "call_leave_queue", channelId);
                        analytics.trackSurface(userId, "call", "leave_queue", channelId);
                    }
                } catch (Exception ignored) {
                }
                var outcome = sessions.end(channelId);
                e.reply(outcome.message()).queue();
            }
            case CallComponentIds.END_CONFIRM -> {
                var outcome = sessions.end(channelId);
                if (outcome.editedInPlace()) {
                    e.reply(ExperienceRenderer.toMessage(
                                    CallPresenter.success("Call ended", "Lobby updated.")
                            ))
                            .setEphemeral(true).queue();
                } else {
                    e.reply(outcome.message()).queue();
                }
            }
            case CallComponentIds.END_CANCEL -> e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.success("Still connected", "Keep talking.")
                    ))
                    .setEphemeral(true).queue();
            case CallComponentIds.PROMPT -> {
                try {
                    if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                        var analytics = com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics();
                        analytics.track(userId, "call_prompt", parsed.sessionId());
                        analytics.trackSurface(userId, "call", "prompt", parsed.sessionId());
                    }
                } catch (Exception ignored) {
                }
                e.reply(ExperienceRenderer.toMessage(
                        CallPresenter.conversationPrompt(
                                CallPresenter.randomPrompt(),
                                parsed.sessionId()
                        )
                )).setEphemeral(true).queue();
            }
            case CallComponentIds.GAME_SHELF -> e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.gameShelf(parsed.sessionId())
                    ))
                    .setEphemeral(true).queue();
            case CallComponentIds.GAME_TTT -> {
                var result = games.proposeTtt(parsed.sessionId(), userId, channelId);
                e.reply(ExperienceRenderer.toMessage(result.success()
                                ? CallPresenter.successInCall("Challenge sent", result.message(), parsed.sessionId())
                                : CallPresenter.warnRecover("Couldn't challenge", result.message())
                        ))
                        .setEphemeral(true).queue();
            }
            case CallComponentIds.GAME_ACCEPT -> {
                String proposerId = parsed.subjectUserId();
                e.getJDA().retrieveUserById(proposerId).queue(
                        proposer -> {
                            var result = games.acceptTtt(
                                    parsed.sessionId(), userId, channelId, e.getUser(), proposer
                            );
                            e.reply(ExperienceRenderer.toMessage(result.success()
                                            ? CallPresenter.gameStarted(parsed.sessionId())
                                            : CallPresenter.warnRecover("Couldn't start", result.message())
                                    ))
                                    .setEphemeral(true).queue();
                        },
                        err -> e.reply(ExperienceRenderer.toMessage(
                                        CallPresenter.warnRecover("Couldn't start", "Proposer not found.")
                                ))
                                .setEphemeral(true).queue()
                );
            }
            case CallComponentIds.GAME_DECLINE -> {
                try {
                    if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                        var analytics = com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics();
                        analytics.track(userId, "call_game_decline", parsed.sessionId());
                        analytics.trackSurface(userId, "call", "game_decline", parsed.sessionId());
                    }
                } catch (Exception ignored) {
                }
                e.reply(ExperienceRenderer.toMessage(
                        CallPresenter.successInCall(
                                "Declined",
                                "You can keep chatting — try a prompt or share profile.",
                                parsed.sessionId()
                        )
                )).setEphemeral(true).queue();
            }
            case CallComponentIds.AGAIN -> {
                try {
                    if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
                        var analytics = com.itsmarsss.callerphone.bootstrap.ApplicationContext.get().analytics();
                        analytics.track(userId, "call_again", channelId);
                        analytics.trackSurface(userId, "call", "again", channelId);
                    }
                } catch (Exception ignored) {
                }
                startAgain(e, userId, channelId);
            }
            default -> e.reply(ExperienceRenderer.toMessage(
                    CallPresenter.warnRecover("That expired", "Open a fresh screen to continue.")
            )).setEphemeral(true).queue();
        }
    }

    private void openReportCategories(ButtonInteraction e, String sessionId) {
        StringSelectMenu.Builder menu = StringSelectMenu.create(CallComponentIds.reportCat(sessionId))
                .setPlaceholder("Choose the closest reason")
                .setRequiredRange(1, 1);
        for (ReportCategory cat : ReportCategory.values()) {
            menu.addOption(cat.label(), cat.code());
        }
        e.reply(ExperienceRenderer.toMessage(CallPresenter.reportPrompt()))
                .addComponents(ActionRow.of(menu.build()))
                .setEphemeral(true)
                .queue();
    }

    private void startAgain(ButtonInteraction e, String userId, String channelId) {
        boolean dm = e.getChannel().getType() == ChannelType.PRIVATE
                || e.getChannel().getType() == ChannelType.GROUP;
        CallEndpoint endpoint = dm
                ? CallEndpoint.dm(channelId, userId)
                : CallEndpoint.guild(channelId, userId);
        var result = sessions.start(endpoint);
        switch (result.status()) {
            case QUEUED -> e.reply(ExperienceRenderer.toMessage(
                            dm
                                    ? CallPresenter.queuedDm(result.queuePosition(), result.queueSize())
                                    : CallPresenter.queued(result.queuePosition(), result.queueSize())
                    ))
                    .queue(hook -> hook.retrieveOriginal().queue(msg ->
                            sessions.rememberLobbyMessage(channelId, msg.getId())));
            case ALREADY_QUEUED -> e.reply(ExperienceRenderer.toMessage(
                            dm
                                    ? CallPresenter.waitingDm(result.queuePosition(), result.queueSize())
                                    : CallPresenter.waiting(result.queuePosition(), result.queueSize())
                    )).queue();
            case MATCHED -> e.reply(sessions.connectedMessage(result.session()))
                    .queue(hook -> hook.retrieveOriginal().queue(msg ->
                            sessions.rememberLobbyMessage(channelId, msg.getId())));
            case CONFLICT -> e.reply(ExperienceRenderer.toMessage(CallPresenter.conflict())).queue();
            case FAILED -> e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.warnRecover("Couldn't connect", result.message())
                    ))
                    .setEphemeral(true).queue();
        }
    }

    private static com.itsmarsss.callerphone.experience.ExperienceView callShareFail(String message) {
        String msg = message == null ? "" : message;
        if (msg.toLowerCase().contains("go live") || msg.toLowerCase().contains("match join")) {
            return com.itsmarsss.callerphone.experience.ExperienceView.builder(
                            com.itsmarsss.callerphone.experience.ExperienceIntent.WARNING)
                    .title("Go live first")
                    .description(msg)
                    .actions(
                            com.itsmarsss.callerphone.experience.ActionSpec.success(
                                    com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                            com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_JOIN_ACCEPT,
                                            "_"
                                    ),
                                    "Create profile"
                            ),
                            com.itsmarsss.callerphone.experience.ActionSpec.primary(
                                    CallComponentIds.prompt("_"),
                                    "Keep talking"
                            )
                    )
                    .ephemeral(true)
                    .build();
        }
        return CallPresenter.warnRecover("Can't complete", msg);
    }

    private void wireShare() {
        if (com.itsmarsss.callerphone.bootstrap.ApplicationContext.isReady()) {
            share.setDecisions(com.itsmarsss.callerphone.bootstrap.ApplicationContext.get()
                    .decisionRepository());
        }
    }

    @Override
    public String getID() {
        return "c";
    }
}
