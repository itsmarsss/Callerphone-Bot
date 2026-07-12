package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Plan §29: product-first about screen. Technical detail lives on /botinfo.
 */
public class About implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.deferReply().queue();
        JDA jda = e.getJDA();
        long servers = 0;
        long users = 0;
        ShardManager sdMgr = Callerphone.sdMgr;
        if (sdMgr != null) {
            for (JDA shard : sdMgr.getShards()) {
                servers += shard.getGuilds().size();
                for (Guild g : shard.getGuilds()) {
                    users += g.getMemberCount();
                }
            }
        }
        e.getHook().editOriginalEmbeds(new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("Callerphone")
                .setDescription(
                        "Meet people, connect communities, and start conversations across Discord.\n\n"
                                + "**" + servers + "** servers · **" + users + "** users\n\n"
                                + "[Invite](" + Callerphone.config.getBotInviteLink() + ") · "
                                + "[Support](" + Callerphone.config.getSupportServer() + ") · "
                                + "[Privacy](" + Callerphone.config.getPrivacyPolicy() + ") · "
                                + "[Terms](" + Callerphone.config.getTermsOfService() + ")"
                )
                .setFooter("Try /help · diagnostics on /botinfo")
                .build()).queue();
    }

    public static String formatUptime(long durationMs) {
        long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(durationMs);
        long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(durationMs) % 24;
        long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60;
        long seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    @Override
    public String getHelp() {
        return "`/about` what Callerphone is";
    }

    @Override
    public String getName() {
        return "about";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "What Callerphone is")
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }
}
