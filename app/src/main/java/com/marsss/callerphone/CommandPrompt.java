package com.marsss.callerphone;

import com.marsss.ICommand;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Scanner;

public class CommandPrompt {
    public static final Logger logger = LoggerFactory.getLogger(CommandPrompt.class);

    private ShardManager sdMgr;

    public void startPrompting() {
        Scanner sc = new Scanner(System.in);

        User selfUser;

        while (true) {
            sdMgr = Callerphone.sdMgr;
            selfUser = Callerphone.selfUser;

            System.out.print("> ");
            String cmd = sc.nextLine();
            if (cmd.startsWith("start")) {
                logger.info("Starting Bot...");
                if (sdMgr != null) {
                    logger.info("Bot Is Already Online.");
                } else {
                    Callerphone.isQuickStart = false;
                    Callerphone.botInit(Callerphone.config.getBotToken(), cmd.replaceFirst("start", ""));
                }
            } else if (cmd.startsWith("quickstart")) {
                logger.info("Quick Starting Bot...");
                if (sdMgr != null) {
                    logger.info("Bot Is Already Online.");
                } else {
                    Callerphone.isQuickStart = true;
                    Callerphone.botInit(Callerphone.config.getBotToken(), cmd.replaceFirst("quickstart", ""));
                }
            } else if (cmd.equals("shutdown")) {
                logger.info("Shutting Down Bot...");
                if (sdMgr != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status")
                            .setColor(new Color(213, 0, 0))
                            .setFooter("Goodbye World...")
                            .setDescription(selfUser.getAsMention() + " is going offline;" + cmd.replaceFirst("shutdown", ""));
                    final TextChannel LOG_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getLogStatusChannel());
                    if (LOG_CHANNEL == null) {
                        logger.error("Error Sending Shutdown Message");
                    } else {
                        LOG_CHANNEL.sendMessageEmbeds(embedBuilder.build()).complete();
                    }
                    sdMgr.shutdown();
                    sdMgr = null;
                }
                logger.info("Bot Offline");
                sc.close();
                System.exit(0);
            } else if (cmd.equals("info")) {
                if (sdMgr.getShards().get(0) != null) {
                    String tag = selfUser.getAsTag();
                    String avatarUrl = selfUser.getAvatarUrl();
                    OffsetDateTime timeCreated = selfUser.getTimeCreated();
                    String id = selfUser.getId();
                    System.out.println("Tag of the bot: " + tag);
                    System.out.println("Avatar url: " + avatarUrl);
                    System.out.println("Time created: " + timeCreated);
                    System.out.println("Id: " + id);
                    System.out.println("Shard Info:");

                    long totalServers = 0;
                    long cachedUsers = 0;
                    long totalUsers = 0;

                    int i = 0;
                    for (JDA jda : sdMgr.getShards()) {
                        totalServers += jda.getGuilds().size();
                        cachedUsers += jda.getUsers().size();

                        long users = 0;

                        for (Guild g : jda.getGuilds()) {
                            users += g.getMemberCount();
                        }

                        totalUsers += users;

                        System.out.println(i);
                        System.out.println("\tShard info: " + jda.getShardInfo().getShardString());
                        System.out.println("\tGuilds: " + jda.getGuilds().size());
                        System.out.println("\tUsers: " + users);
                        i++;
                    }

                    System.out.println("Total Guilds: " + totalServers);
                    System.out.println("Total Cached Users: " + cachedUsers);
                    System.out.println("Total Users: " + totalUsers);
                    continue;
                }
                logger.info("Bot Is Offline");
            } else if (cmd.equals("updateCMD")) {
                upsert();
                System.out.println("Done Upserting");
            } else if (cmd.equals("help")) {
                System.out.println(
                        "Option 1: start <msg> = To start the bot\n" +
                                "Option 2: quickstart <msg> = To start the bot quicker" +
                                "Option 3: shutdown <msg> = To shutdown the bot\n" +
                                "Option 4: info = To get info of the bot\n" +
                                "Option 5: updateCMD = Update all slash commands\n" +
                                "Option 6: help = CBCL help (this)\n\n");
            } else {
                logger.warn("Unknown Command");
            }
        }
    }

    private void upsert() {
        for (JDA jda : sdMgr.getShards()) {
            CommandListUpdateAction commands = jda.updateCommands();

            for (ICommand command : Callerphone.cmdLst) {
                if (!ISlashCommand.class.isAssignableFrom(command.getClass())) {
                    continue;
                }

                SlashCommandData commandData = ((ISlashCommand) (command)).getCommandData();
                if (commandData == null) {
                    continue;
                }

                commands.addCommands(commandData);
            }

            commands.queue();
        }
    }
}
