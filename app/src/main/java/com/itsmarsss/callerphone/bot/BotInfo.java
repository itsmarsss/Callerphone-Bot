package com.itsmarsss.callerphone.bot;

import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.format.DateTimeFormatter;

public class BotInfo implements ISlashCommand {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        e.deferReply(true).queue();
        JDA jda = e.getJDA();

        String base = "**Tag of the bot:** " + jda.getSelfUser().getAsTag()
                + "\n**Avatar url:** [link](" + jda.getSelfUser().getEffectiveAvatarUrl() + ")"
                + "\n**Time created:** " + DATE_FMT.format(jda.getSelfUser().getTimeCreated())
                + "\n**Id:** " + jda.getSelfUser().getId()
                + "\n**Shard info:** [" + (jda.getShardInfo().getShardId() + 1) + "/"
                + jda.getShardInfo().getShardTotal() + "]"
                + "\n**Servers:** " + jda.getGuilds().size()
                + "\n**WS ping:** " + jda.getGatewayPing() + "ms";

        jda.getRestPing().queue(
                ping -> e.getHook().editOriginalEmbeds(new EmbedBuilder()
                        .setColor(ToolSet.COLOR)
                        .setTitle("**Bot Info**")
                        .setDescription(base + "\n**Rest ping:** " + ping + "ms")
                        .build()).queue(),
                err -> e.getHook().editOriginalEmbeds(new EmbedBuilder()
                        .setColor(ToolSet.COLOR)
                        .setTitle("**Bot Info**")
                        .setDescription(base + "\n**Rest ping:** *Unable to obtain*")
                        .build()).queue()
        );
    }

    @Override
    public String getHelp() {
        return "`/botinfo` - Get information about the bot.";
    }

    @Override
    public String getName() {
        return "botinfo";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get information about the bot")
                .setContexts(InteractionContextType.GUILD);
    }
}
