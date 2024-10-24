package com.marsss.callerphone;

import com.marsss.callerphone.channelpool.ChannelPool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
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


        commands.addCommands(
                Commands.slash("profile", "Get your profile").addOptions(
                                new OptionData(OptionType.USER, "target", "Target user").setRequired(true)
                        )
                        .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("chat", "Chat with people from other servers")
                        .addSubcommands(
                                new SubcommandData("default", "Chat with people from other servers"),
                                new SubcommandData("anonymous", "Chat anonymously"),
                                new SubcommandData("familyfriendly", "Chat with swear word censoring"),
                                new SubcommandData("ffandanon", "Chat family friendly and anonymously")
                        )
                        .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("endchat", "End chatting with people from another server")
                        .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("prefix", "Set in text prefix")
                        .addOptions(
                                new OptionData(OptionType.STRING, "prefix", "Set prefix").setRequired(true)
                        )
                        .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("reportchat", "Report a chat with people from another server")
                        .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("endpool", "End a channel pool")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("hostpool", "Host a channel pool")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("joinpool", "Join a channel pool")
                        .addOptions(
                                new OptionData(OptionType.STRING, "hostid", "Host channel's ID").setRequired(true),
                                new OptionData(OptionType.STRING, "password", "Channel pool password (if given)")
                        )
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("leavepool", "Leave a channel pool")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("poolparticipants", "View channels in a channel pool")
                        .setGuildOnly(true)
        );

        commands.addCommands(
          Commands.slash("poolsettings", "Configure this pool")
                  .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("poolcap", "Set pool capacity")
                        .addOptions(
                                new OptionData(OptionType.INTEGER, "capacity", "Pool capacity")
                                        .setMinValue(2)
                                        .setMaxValue(10)
                                        .setRequired(true)
                        )
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("poolkick", "Kick channel from pool")
                        .addOptions(
                                new OptionData(OptionType.STRING, "target", "Target channel").setRequired(true)
                        )
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("poolpublicity", "Change visibility of pool")
                        .addOptions(
                                new OptionData(OptionType.BOOLEAN, "public", "Publicity of pool").setRequired(true)
                        )
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("poolpassword", "Change password of pool")
                        .addOptions(
                                new OptionData(OptionType.STRING, "password", "Password of pool").setRequired(true)
                        )
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
        );

        commands.addCommands(
                Commands.slash("game", "Play minigames")
                        .addSubcommands(
                                new SubcommandData("tictactoe", "Play TicTacToe with someone")
                                        .addOptions(
                                                new OptionData(OptionType.USER, "opponent", "Who to challenge")
                                                        .setRequired(true)
                                        )
                        )
                        .setGuildOnly(true)
        );

        commands.addCommands(

        );

        commands.addCommands(

        );

        commands.addCommands(
                Commands.slash("sendbottle", "Send a message in bottle")
                        .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("findbottle", "Find a message in bottle")
                        .setGuildOnly(true)
        );

        commands.queue();
    }
}
