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
                e.getMessage().replyEmbeds(CallEmbeds.success("Report received", "Thanks for reporting.")).queue();
                return;
            }
            e.replyEmbeds(CallEmbeds.warn("That expired", "Open a fresh screen to continue."))
                    .setEphemeral(true).queue();
            return;
        }
        wireShare();
        String userId = e.getUser().getId();
        String channelId = e.getChannel().getId();
        switch (parsed.action()) {
            case CallComponentIds.SHARE -> {
                var result = share.share(parsed.sessionId(), userId, channelId);
                e.replyEmbeds(result.success()
                                ? CallEmbeds.success("Profile shared", result.message())
                                : CallEmbeds.warn("Can't share", result.message()))
                        .setEphemeral(true).queue();
            }
            case CallComponentIds.LIKE -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), true, channelId);
                e.replyEmbeds(result.success()
                                ? CallEmbeds.success("Interest sent", result.message())
                                : CallEmbeds.warn("Couldn't send", result.message()))
                        .setEphemeral(true).queue();
            }
            case CallComponentIds.PASS -> {
                var result = share.react(parsed.sessionId(), userId, parsed.subjectUserId(), false, channelId);
                e.replyEmbeds(CallEmbeds.info("Noted", result.message())).setEphemeral(true).queue();
            }
            case CallComponentIds.REPORT -> openReportCategories(e, parsed.sessionId());
            case CallComponentIds.LEAVE_QUEUE -> {
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
            case CallComponentIds.PROMPT -> e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.conversationPrompt(CallPresenter.randomPrompt())
                    ))
                    .setEphemeral(true).queue();
            case CallComponentIds.GAME_SHELF -> e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.gameShelf(parsed.sessionId())
                    ))
                    .setEphemeral(true).queue();
            case CallComponentIds.GAME_TTT -> {
                var result = games.proposeTtt(parsed.sessionId(), userId, channelId);
                e.reply(ExperienceRenderer.toMessage(result.success()
                                ? CallPresenter.success("Challenge sent", result.message())
                                : CallPresenter.warn("Couldn't challenge", result.message())
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
                                            ? CallPresenter.success("Game on", result.message())
                                            : CallPresenter.warn("Couldn't start", result.message())
                                    ))
                                    .setEphemeral(true).queue();
                        },
                        err -> e.reply(ExperienceRenderer.toMessage(
                                        CallPresenter.warn("Couldn't start", "Proposer not found.")
                                ))
                                .setEphemeral(true).queue()
                );
            }
            case CallComponentIds.GAME_DECLINE -> e.reply(ExperienceRenderer.toMessage(
                            CallPresenter.success("Declined", "You can keep chatting on the call.")
                    ))
                    .setEphemeral(true).queue();
            case CallComponentIds.AGAIN -> startAgain(e, userId, channelId);
            default -> e.replyEmbeds(CallEmbeds.warn("That expired", "Open a fresh screen to continue."))
                    .setEphemeral(true).queue();
        }
    }

    private void openReportCategories(ButtonInteraction e, String sessionId) {
        StringSelectMenu.Builder menu = StringSelectMenu.create(CallComponentIds.reportCat(sessionId))
                .setPlaceholder("Choose the closest reason")
                .setRequiredRange(1, 1);
        for (ReportCategory cat : ReportCategory.values()) {
            menu.addOption(cat.label(), cat.code());
        }
        e.replyEmbeds(CallEmbeds.warn(
                        "Report this call",
                        "Choose the closest reason. Recent messages will be attached for review."
                ))
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
            case CONFLICT -> e.replyEmbeds(CallEmbeds.conflict()).setEphemeral(true).queue();
            case FAILED -> e.replyEmbeds(CallEmbeds.warn("Couldn't connect", result.message()))
                    .setEphemeral(true).queue();
        }
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
