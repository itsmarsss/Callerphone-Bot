package com.marsss.callerphone;

import com.marsss.ICommand;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Scanner;

public class CommandPrompt {

    public static final Logger logger = LoggerFactory.getLogger(CommandPrompt.class);

    private JDA jda = Callerphone.jda;

    public void startPrompting() throws InterruptedException {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String cmd = sc.nextLine();
            if (cmd.startsWith("start")) {
                logger.info("Starting Bot...");
                if (jda != null) {
                    logger.info("Bot Is Already Online.");
                } else {
                    Callerphone.isQuickStart = false;
                    Callerphone.botInit(Callerphone.config.getBotToken(), cmd.replaceFirst("start", ""));
                }
            } else if (cmd.startsWith("quickstart")) {
                logger.info("Quick Starting Bot...");
                if (jda != null) {
                    logger.info("Bot Is Already Online.");
                } else {
                    Callerphone.isQuickStart = true;
                    Callerphone.botInit(Callerphone.config.getBotToken(), cmd.replaceFirst("quickstart", ""));
                }
            } else if (cmd.equals("shutdown")) {
                logger.info("Shutting Down Bot...");
                if (jda != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status")
                            .setColor(new Color(213, 0, 0))
                            .setFooter("Goodbye World...")
                            .setDescription(jda.getSelfUser().getAsMention() + " is going offline;" + cmd.replaceFirst("shutdown", ""));
                    final TextChannel LOG_CHANNEL = ToolSet.getTextChannel(Callerphone.config.getLogStatusChannel());
                    if (LOG_CHANNEL == null) {
                        logger.error("Error Sending Shutdown Message");
                    } else {
                        LOG_CHANNEL.sendMessageEmbeds(embedBuilder.build()).complete();
                    }
                    jda.awaitReady();
                    jda.shutdown();
                    jda = null;
                }
                logger.info("Bot Offline");
                sc.close();
                System.exit(0);
            } else if (cmd.equals("info")) {
                if (jda != null) {
                    String tag = jda.getSelfUser().getAsTag();
                    String avatarUrl = jda.getSelfUser().getAvatarUrl();
                    OffsetDateTime timeCreated = jda.getSelfUser().getTimeCreated();
                    String id = jda.getSelfUser().getId();
                    System.out.println("Tag of the bot: " + tag);
                    System.out.println("Avatar url: " + avatarUrl);
                    System.out.println("Time created: " + timeCreated);
                    System.out.println("Id: " + id);
                    System.out.println("Shard info: " + jda.getShardInfo().getShardString());
                    System.out.println("Guilds: " + jda.getGuilds().size());
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
        CommandListUpdateAction commands = Callerphone.jda.updateCommands();

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
