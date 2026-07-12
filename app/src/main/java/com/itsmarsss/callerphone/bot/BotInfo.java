package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.experience.ExperienceIntent;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.experience.ExperienceView;
import com.itsmarsss.callerphone.experience.ViewField;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/** Plan §30: operational status without host CPU/memory dump. */
public class BotInfo implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.deferReply(true).queue();
        JDA jda = e.getJDA();
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        jda.getRestPing().queue(
                ping -> e.getHook().editOriginal(ExperienceRenderer.toEdit(build(jda, ping, uptimeMs))).queue(),
                err -> e.getHook().editOriginal(ExperienceRenderer.toEdit(build(jda, -1, uptimeMs))).queue()
        );
    }

    private ExperienceView build(JDA jda, long restPing, long uptimeMs) {
        List<ViewField> fields = new ArrayList<>();
        fields.add(ViewField.of(
                "Latency",
                restPing < 0 ? "—" : restPing + " ms rest · " + jda.getGatewayPing() + " ms WS"
        ));
        fields.add(ViewField.of("Servers", String.valueOf(jda.getGuilds().size())));
        fields.add(ViewField.of("Version", Callerphone.VERSION));
        fields.add(ViewField.of("Uptime", About.formatUptime(uptimeMs)));
        fields.add(ViewField.of(
                "Shard",
                (jda.getShardInfo().getShardId() + 1) + "/" + jda.getShardInfo().getShardTotal()
        ));
        fields.add(ViewField.of(
                "Match",
                ApplicationContext.isReady() ? "Ready" : "Starting"
        ));
        String footer = "Support: " + (Callerphone.config != null ? Callerphone.config.getSupportServer() : "");
        return ExperienceView.builder(ExperienceIntent.NEUTRAL)
                .title("Callerphone status")
                .description("Online · product surfaces healthy")
                .fields(fields)
                .footer(footer)
                .actions(
                        com.itsmarsss.callerphone.experience.ActionSpec.success(
                                com.itsmarsss.callerphone.match.component.MatchComponentIds.of(
                                        com.itsmarsss.callerphone.match.component.MatchComponentIds.ACTION_START_BROWSE, "_"
                                ),
                                "Discover people"
                        ),
                        com.itsmarsss.callerphone.experience.ActionSpec.secondary(
                                com.itsmarsss.callerphone.call.discord.CallComponentIds.again("_"),
                                "Start a call"
                        ),
                        com.itsmarsss.callerphone.experience.ActionSpec.secondary(
                                com.itsmarsss.callerphone.msginbottle.BottleComponentIds.of(
                                        com.itsmarsss.callerphone.msginbottle.BottleComponentIds.ACTION_FIND, "_"
                                ),
                                "Find a bottle"
                        )
                )
                .build();
    }

    @Override
    public String getHelp() {
        return "`/botinfo` operational status";
    }

    @Override
    public String getName() {
        return "botinfo";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Operational status")
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
