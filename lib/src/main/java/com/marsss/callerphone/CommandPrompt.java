package com.marsss.callerphone;

import com.marsss.callerphone.channelpool.ChannelPool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Scanner;

public class CommandPrompt {

    public static final Logger logger = LoggerFactory.getLogger(CommandPrompt.class);

    private JDA jda = Callerphone.jda;

    public void startPrompting() throws InterruptedException {
        Scanner sc = new Scanner(System.in);

        while (true) {
            String cmd = sc.nextLine();
            if (cmd.startsWith("start")) {
                logger.info("Starting Bot...");
                if (jda != null) {
                    logger.info("Bot Is Online Right Now");
                } else {
                    Callerphone.isQuickStart = false;
                    Callerphone.BotInit(Callerphone.config.getBotToken(), cmd.replaceFirst("start", ""), false);
                }
            } else if (cmd.startsWith("quickstart")) {
                logger.info("Starting Bot...");
                if (jda != null) {
                    logger.info("Bot Is Online Right Now");
                } else {
                    Callerphone.isQuickStart = true;
                    Callerphone.BotInit(Callerphone.config.getBotToken(), cmd.replaceFirst("quickstart", ""), true);
                }
            } else if (cmd.equals("shutdown")) {
                logger.info("Shutting Down Bot...");
                if (jda != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status").setColor(new Color(213, 0, 0)).setFooter("Goodbye World...").setDescription(jda.getSelfUser().getAsMention() + " is going offline;" + cmd.replaceFirst("shutdown", ""));
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
            } else if (cmd.equals("presence")) {
                if (jda == null) {
                    logger.info("Bot Is Offline");
                    continue;
                }
                setActivity(sc);
                logger.info("Bot Is Offline");

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
            } else if (cmd.equals("recal")) {
                logger.info("Recalibrating...");
                try {
                    Storage.readData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.info("Done recalibration!");
            } else if (cmd.equals("poolnum")) {
                System.out.println("Currently there are " + ChannelPool.config.size() + " channel pools running.");
            } else if (cmd.equals("updateCMD")) {
                update();
                System.out.println("Done Updating");
            } else if (cmd.equals("upsertCMD")) {
                upsert();
                System.out.println("Done Upserting");
            } else if (cmd.equals("help")) {
                System.out.println(
                        "Option 1: start <msg> = To start the bot\n" +
                                "Option 2: shutdown = To shutdown the bot\n" +
                                "Option 3: presence = To set presence\n" +
                                "Option 4: info = To get info of the bot\n" +
                                "Option 5: recal = To read resources again\n" +
                                "Option 6: poolnum = To see number of running pools\n" +
                                "Option 7: updateCMD = Remove all slash commands\n" +
                                "Option 8: upsertCMD = Upsert all slash commands\n" +
                                "Option 9: help = UBCL help (this)\n\n" +
                                "Other: quickstart <msg> = To start the bot quicker");
            } else {
                logger.warn("Unknown Command");
            }
        }
    }

    private void upsert() {
        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash()
        );

        jda.upsertCommand(new CommandData("about", "About Callerphone")).queue();
        jda.upsertCommand(new CommandData("donate", "Help us out by donating")).queue();
        jda.upsertCommand(new CommandData("invite", "Invite Callerphone")).queue();
        jda.upsertCommand(new CommandData("ping", "Get the bot's ping")).queue();
        jda.upsertCommand(new CommandData("profile", "Get your profile")
                .addOptions(
                        new OptionData(OptionType.USER, "target", "Target user").setRequired(true)
                )).queue();
        jda.upsertCommand(new CommandData("uptime", "Get the bot's uptime")).queue();

        jda.upsertCommand(new CommandData("chat", "Chat with people from other servers")
                        .addSubcommands(
                                new SubcommandData("default", "Chat with people from other servers"),
                                new SubcommandData("anonymous", "Chat anonymously"),
                                new SubcommandData("familyfriendly", "Chat with swear word censoring"),
                                new SubcommandData("ffandanon", "Chat family friendly and anonymously")))
                .queue();
        jda.upsertCommand(new CommandData("endchat", "End chatting with people from another server")).queue();
        jda.upsertCommand(new CommandData("prefix", "Set in text prefix")
                .addOptions(
                        new OptionData(OptionType.STRING, "prefix", "Set prefix").setRequired(true)
                )).queue();
        jda.upsertCommand(new CommandData("reportchat", "Report a chat with people from another server")).queue();

        jda.upsertCommand(new CommandData("endpool", "End a channel pool")).queue();
        jda.upsertCommand(new CommandData("hostpool", "Host a channel pool")).queue();
        jda.upsertCommand(new CommandData("joinpool", "Join a channel pool")
                .addOptions(
                        new OptionData(OptionType.STRING, "hostid", "Host channel's ID").setRequired(true),
                        new OptionData(OptionType.STRING, "password", "Channel pool password (if given)")
                )
        ).queue();
        jda.upsertCommand(new CommandData("leavepool", "Leave a channel pool")).queue();
        jda.upsertCommand(new CommandData("poolparticipants", "View channels in a channel pool")).queue();
        jda.upsertCommand(new CommandData("poolcap", "Set pool capacity")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "capacity", "Pool capacity")
                                .setMinValue(2)
                                .setMaxValue(10)
                                .setRequired(true)
                )
        ).queue();
        jda.upsertCommand(new CommandData("poolkick", "Kick channel from pool")
                .addOptions(
                        new OptionData(OptionType.STRING, "target", "Target channel").setRequired(true)
                )
        ).queue();
        jda.upsertCommand(new CommandData("poolpublicity", "Change visibility of pool")
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "public", "Publicity of pool").setRequired(true)
                )
        ).queue();
        jda.upsertCommand(new CommandData("poolpassword", "Change password of pool")
                .addOptions(
                        new OptionData(OptionType.STRING, "password", "Password of pool").setRequired(true)
                )
        ).queue();

        jda.upsertCommand(new CommandData("channelinfo", "Get a channel's information")
                .addOptions(
                        new OptionData(OptionType.CHANNEL, "channel", "Target channel")
                                .setRequired(true)
                )
        ).queue();

        jda.upsertCommand(new CommandData("help", "Get some help")
                .addOptions(
                        new OptionData(OptionType.STRING, "term", "Search term")
                )
        ).queue();

        jda.upsertCommand(new CommandData("roleinfo", "Get a role's information")
                .addOptions(
                        new OptionData(OptionType.ROLE, "role", "Target role")
                                .setRequired(true)
                )
        ).queue();

        jda.upsertCommand(new CommandData("search", "Browse the internet from Discord")
                .addOptions(
                        new OptionData(OptionType.STRING, "query", "Search query")
                                .setRequired(true)
                )
        ).queue();

        jda.upsertCommand(new CommandData("serverinfo", "Get this server's information")).queue();

        jda.upsertCommand(new CommandData("userinfo", "Get a user's information")
                .addOptions(
                        new OptionData(OptionType.USER, "member", "Target member")
                                .setRequired(true)
                )
        ).queue();
    }

    private void setActivity(Scanner sc) {
        Activity act;
        logger.info("Change Presence...");
        try {
            label:
            while (true) {

                System.out.println("Activity: ");
                String msg = sc.next().toLowerCase();

                switch (msg) {
                    case "<rs>":
                        act = null;
                        break label;
                    case "competing":
                        System.out.println("Status Message: ");
                        sc.nextLine();
                        String comp = sc.nextLine();
                        System.out.println("Competing: " + comp);
                        act = Activity.competing(comp);
                        break label;

                    case "listening":
                        System.out.println("Status Message: ");
                        sc.nextLine();
                        String song = sc.nextLine();
                        System.out.println("Listening: " + song);
                        act = Activity.listening(song);
                        break label;

                    case "playing":
                        System.out.println("Status Message: ");
                        sc.nextLine();
                        String game = sc.nextLine();
                        System.out.println("Playing: " + game);
                        act = Activity.playing(game);
                        break label;

                    case "streaming":
                        System.out.println("Title Message: ");
                        sc.nextLine();
                        String title = sc.nextLine();
                        System.out.println("Stream Link: ");
                        String link = sc.nextLine();
                        System.out.println("Title: " + title + "\n" + "Link: " + link);
                        act = Activity.streaming(title, link);
                        break label;

                    case "watching":
                        System.out.println("Status Message: ");
                        sc.nextLine();
                        String watch = sc.nextLine();
                        System.out.println("Watching: " + watch);
                        act = Activity.watching(watch);
                        break label;
                }
            }

            OnlineStatus s;

            while (true) {
                System.out.println("Online Status: ");
                String msg = sc.next().toLowerCase();

                if (msg.toLowerCase().startsWith("onl")) {
                    s = OnlineStatus.ONLINE;
                    break;

                } else if (msg.toLowerCase().startsWith("idl")) {
                    s = OnlineStatus.IDLE;
                    break;

                } else if (msg.toLowerCase().startsWith("dnd")) {
                    s = OnlineStatus.DO_NOT_DISTURB;
                    break;

                } else if (msg.toLowerCase().startsWith("inv")) {
                    s = OnlineStatus.INVISIBLE;
                    break;

                }
            }
            jda.getPresence().setPresence(s, act);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Input error, please try again");
        }
    }

}
