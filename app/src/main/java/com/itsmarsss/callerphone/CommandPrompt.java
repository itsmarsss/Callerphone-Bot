package com.itsmarsss.callerphone;

import com.itsmarsss.ICommand;
import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.Scanner;

public class CommandPrompt {
    public static final Logger logger = LoggerFactory.getLogger(CommandPrompt.class);

    private static final String HELP_TEXT =
            "Commands:\n" +
                    "  start <msg>      - Start the bot\n" +
                    "  quickstart <msg> - Start with reduced caching\n" +
                    "  shutdown <msg>   - Shutdown the bot\n" +
                    "  info             - Print bot/shard stats\n" +
                    "  updateCMD        - Upsert all slash commands\n" +
                    "  help             - Show this help\n";

    public void startPrompting() {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) {
                    break;
                }

                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("start")) {
                    startBot(false, line.substring("start".length()).trim());
                } else if (line.startsWith("quickstart")) {
                    startBot(true, line.substring("quickstart".length()).trim());
                } else if (line.startsWith("shutdown")) {
                    shutdown(line.substring("shutdown".length()).trim());
                    return;
                } else if (line.equals("info")) {
                    printInfo();
                } else if (line.equals("updateCMD")) {
                    upsert();
                    System.out.println("Done upserting slash commands.");
                } else if (line.equals("help")) {
                    System.out.println(HELP_TEXT);
                } else {
                    logger.warn("Unknown command. Type 'help' for options.");
                }
            }
        }
    }

    private void startBot(boolean quick, String startupMsg) {
        if (Callerphone.sdMgr != null) {
            logger.info("Bot is already online.");
            return;
        }
        logger.info(quick ? "Quick-starting bot..." : "Starting bot...");
        Callerphone.isQuickStart = quick;
        Callerphone.botInit(Callerphone.config.getBotToken(), startupMsg);
    }

    private void shutdown(String message) {
        logger.info("Shutting down bot...");
        ShardManager sdMgr = Callerphone.sdMgr;
        User selfUser = Callerphone.selfUser;

        if (sdMgr != null) {
            if (selfUser != null) {
                TextChannel logChannel = ToolSet.getTextChannel(Callerphone.config.getLogStatusChannel());
                if (logChannel != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Status")
                            .setColor(new Color(213, 0, 0))
                            .setFooter("Goodbye World...")
                            .setDescription(selfUser.getAsMention() + " is going offline; " + message);
                    try {
                        logChannel.sendMessageEmbeds(embed.build()).complete();
                    } catch (Exception e) {
                        logger.error("Failed to send shutdown message", e);
                    }
                } else {
                    logger.error("Invalid log status channel; skip shutdown message");
                }
            }
            sdMgr.shutdown();
            Callerphone.sdMgr = null;
            Callerphone.dbConnector.close();
        }

        logger.info("Bot offline");
        System.exit(0);
    }

    private void printInfo() {
        ShardManager sdMgr = Callerphone.sdMgr;
        User selfUser = Callerphone.selfUser;

        if (sdMgr == null || selfUser == null || sdMgr.getShards().isEmpty()) {
            logger.info("Bot is offline");
            return;
        }

        System.out.println("Tag: " + selfUser.getAsTag());
        System.out.println("Id: " + selfUser.getId());
        System.out.println("Avatar: " + selfUser.getAvatarUrl());
        System.out.println("Created: " + selfUser.getTimeCreated());
        System.out.println("Shards:");

        long totalServers = 0;
        long cachedUsers = 0;
        long totalUsers = 0;
        int i = 0;

        for (JDA jda : sdMgr.getShards()) {
            long guildUsers = 0;
            for (Guild g : jda.getGuilds()) {
                guildUsers += g.getMemberCount();
            }

            totalServers += jda.getGuilds().size();
            cachedUsers += jda.getUsers().size();
            totalUsers += guildUsers;

            System.out.println("  [" + i + "] " + jda.getShardInfo().getShardString()
                    + " guilds=" + jda.getGuilds().size()
                    + " users=" + guildUsers
                    + " status=" + jda.getStatus());
            i++;
        }

        System.out.println("Total guilds: " + totalServers);
        System.out.println("Total cached users: " + cachedUsers);
        System.out.println("Total members: " + totalUsers);
    }

    private void upsert() {
        ShardManager sdMgr = Callerphone.sdMgr;
        if (sdMgr == null) {
            logger.warn("Cannot upsert commands while bot is offline");
            return;
        }

        for (JDA jda : sdMgr.getShards()) {
            CommandListUpdateAction commands = jda.updateCommands();
            int count = 0;
            for (ICommand command : Callerphone.cmdLst) {
                if (!(command instanceof ISlashCommand)) {
                    continue;
                }
                SlashCommandData data = ((ISlashCommand) command).getCommandData();
                if (data == null) {
                    continue;
                }
                commands.addCommands(data);
                count++;
            }
            final int registered = count;
            commands.queue(
                    success -> logger.info("Upserted {} commands on shard {}", registered, jda.getShardInfo()),
                    error -> logger.error("Failed to upsert commands on shard {}", jda.getShardInfo(), error)
            );
        }
    }
}
