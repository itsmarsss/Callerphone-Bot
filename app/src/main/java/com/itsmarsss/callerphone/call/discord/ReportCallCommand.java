package com.itsmarsss.callerphone.call.discord;

import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.safety.ReportCategory;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Plan §19: category select, then preserve transcript on submit (via button/select handlers).
 */
public final class ReportCallCommand implements ISlashCommand {
    private final CallSessionService calls = CallSessionService.get();

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        String channelId = e.getChannel().getId();
        var session = calls.get(channelId);
        if (session.isEmpty()) {
            e.reply(ExperienceRenderer.toMessage(CallPresenter.warnRecover(
                    "No recent call to report",
                    "Reports are available during a call and shortly after it ends."
            ))).setEphemeral(true).queue();
            return;
        }
        String sessionId = session.get().getId();
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

    @Override
    public String getHelp() {
        return "`/reportcall` report the current call";
    }

    @Override
    public String getName() {
        return "reportcall";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Report the current call")
                .setContexts(
                        InteractionContextType.GUILD,
                        InteractionContextType.BOT_DM,
                        InteractionContextType.PRIVATE_CHANNEL
                );
    }
}
