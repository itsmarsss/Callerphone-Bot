package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.lang.management.ManagementFactory;

/** Plan §30: operational status without host CPU/memory dump. */
public class BotInfo implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.deferReply(true).queue();
        JDA jda = e.getJDA();
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        jda.getRestPing().queue(
                ping -> e.getHook().editOriginalEmbeds(build(jda, ping, uptimeMs)).queue(),
                err -> e.getHook().editOriginalEmbeds(build(jda, -1, uptimeMs)).queue()
        );
    }

    private net.dv8tion.jda.api.entities.MessageEmbed build(JDA jda, long restPing, long uptimeMs) {
        return new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("Callerphone status")
                .setDescription("Online · All systems operational")
                .addField("Latency", restPing < 0 ? "—" : restPing + " ms rest · " + jda.getGatewayPing() + " ms WS", true)
                .addField("Servers", String.valueOf(jda.getGuilds().size()), true)
                .addField("Version", Callerphone.VERSION, true)
                .addField("Uptime", About.formatUptime(uptimeMs), true)
                .addField("Shard", (jda.getShardInfo().getShardId() + 1) + "/" + jda.getShardInfo().getShardTotal(), true)
                .setFooter("Support: " + (Callerphone.config != null ? Callerphone.config.getSupportServer() : ""))
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
