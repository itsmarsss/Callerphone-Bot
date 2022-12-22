package com.marsss.callerphone;

import java.awt.Color;
import java.io.*;
import java.net.URLDecoder;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.marsss.callerphone.channelpool.commands.*;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import com.marsss.callerphone.tccallerphone.TCCallerphoneListener;
import com.marsss.callerphone.tccallerphone.commands.Chat;
import com.marsss.callerphone.tccallerphone.commands.EndChat;
import com.marsss.callerphone.tccallerphone.commands.ReportChat;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marsss.ICommand;
import com.marsss.callerphone.bot.*;
import com.marsss.callerphone.utils.*;
import com.marsss.callerphone.listeners.*;
import com.marsss.callerphone.channelpool.*;
import com.marsss.callerphone.tccallerphone.ConvoStorage;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Callerphone {

    public static final Logger logger = LoggerFactory.getLogger(Callerphone.class);

    public static String parent;

    public static LinkedList<String> blacklist = new LinkedList<>();
    public static HashMap<String, String> prefix = new HashMap<>();
    public static LinkedList<String> admin = new LinkedList<>();

    public static LinkedList<String> filter = new LinkedList<>();

    public static HashMap<String, ICommand> cmdMap = new HashMap<>();

    public static final String Prefix = "c?";

    public static String brainURL = "http://api.brainshop.ai/get?bid=160403&key=FFFNOBQEMnANoVn1&uid=[uid]&msg=[msg]";
    public static String Callerphone = "<:CallerphoneEmote:899051549173637120> ";
    public static String CallerphoneCall = "<:Pog:892780255452987402> ";
    public static String logstatus = "852338750519640116";
    public static String reportchannel = "897290511000404008";
    public static String invite = "https://discord.com/oauth2/authorize?client_id=849713468348956692&permissions=414464724040&scope=bot%20applications.commands";
    public static String support = "https://discord.gg/jcYKsfw48p";
    public static String tunessupport = "https://discord.gg/TyHaxtWAmX";
    public static String donate = "https://www.patreon.com/itsmarsss";
    public static String owner = "841028865995964477";

    public static boolean isQuickStart;

    public static JDA jda;

    private static HashMap<String, Long> userCredits = new HashMap<String, Long>();
    private static HashMap<String, Long> userExecuted = new HashMap<String, Long>();
    private static HashMap<String, Long> userTransmitted = new HashMap<String, Long>();
    public static final String ERROR_MSG = "An error occurred with error: `%s`." +
            "\nIf this is a recurring problem, please join our support server and report this issue. " + support;

    private static final EnumSet<GatewayIntent> intent = EnumSet.of(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_INVITES,
            GatewayIntent.DIRECT_MESSAGES);

    private static void BotInit(String token, String startupmsg, boolean quickStart) {

        try {
            if (quickStart) {
                jda = JDABuilder.createDefault(token, intent)
                        .enableCache(CacheFlag.VOICE_STATE)
                        .enableCache(CacheFlag.ROLE_TAGS)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build();
            } else {
                jda = JDABuilder.createDefault(token, intent)
                        .enableCache(CacheFlag.VOICE_STATE)
                        .enableCache(CacheFlag.ROLE_TAGS)
                        .setChunkingFilter(ChunkingFilter.ALL)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build();
            }

            ArrayList<ICommand> cmdLst = new ArrayList<>();
            cmdLst.add(new About());
            cmdLst.add(new Donate());
            cmdLst.add(new Invite());
            cmdLst.add(new Ping());
            cmdLst.add(new Profile());
            cmdLst.add(new Uptime());


            cmdLst.add(new BotInfo());
            cmdLst.add(new ChannelInfo());
            cmdLst.add(new Colour());
            cmdLst.add(new Help());
            cmdLst.add(new RoleInfo());
            cmdLst.add(new Search());
            cmdLst.add(new ServerInfo());
            cmdLst.add(new UserInfo());

            cmdLst.add(new Chat());
            cmdLst.add(new EndChat());
            cmdLst.add(new Prefix());
            cmdLst.add(new ReportChat());

            cmdLst.add(new HostPool());
            cmdLst.add(new JoinPool());
            cmdLst.add(new EndPool());
            cmdLst.add(new LeavePool());
            cmdLst.add(new PoolParticipants());
            cmdLst.add(new PoolCap());
            cmdLst.add(new PoolPub());
            cmdLst.add(new PoolPwd());
            cmdLst.add(new PoolKick());

            for (ICommand cmd : cmdLst) {
                for (String trigger : cmd.getTriggers()) {
                    cmdMap.put(trigger, cmd);
                    System.out.println("Put: key=" + trigger + ", value=" + cmd.getClass().getName());
                }
            }

            jda.addEventListener(new CommandListener());
            jda.addEventListener(new OnOtherEvent());
            jda.addEventListener(new OnSlashCommand());
            jda.addEventListener(new OnPrivateMessage());
            jda.addEventListener(new TCCallerphoneListener());
            jda.addEventListener(new ChannelPoolListener());


            for (int i = 0; i < 10000; i++) {
                TCCallerphone.convos.add(new ConvoStorage(new ConcurrentLinkedQueue<>(), "empty", "", 0, true, true, false, false, false));
            }

            jda.awaitReady();

            jda.getPresence().setActivity(Activity.watching("for " + Prefix + "help"));
            logger.info("Bot online");

            System.out.println("\nGuild List: ");
            for (Guild g : jda.getGuilds()) {
                System.out.println("- " + g.getName());
            }


            parent = URLDecoder.decode(new File(Callerphone.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath(), "UTF-8");
            System.out.println("\nParent - " + parent);


            try {
                getInfo(new File(parent + "/info.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with info.txt");
                logger.warn("Critical Issues May Appear (BrainURL and other links)");
            }

            try {
                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status").setColor(new Color(24, 116, 52)).setFooter("Hello World!").setDescription(jda.getSelfUser().getAsMention() + " is now online;" + startupmsg);
                jda.getTextChannelById(logstatus).sendMessageEmbeds(embedBuilder.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error Sending Startup Message");
            }

            try {
                getBlack(new File(parent + "/blacklist.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with blacklist.txt");
            }

            try {
                getPrefix(new File(parent + "/prefix.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with prefix.txt");
            }

            try {
                getAdmin(new File(parent + "/admin.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with admin.txt");
            }

            try {
                getFilter(new File(parent + "/filter.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with filter.txt");
            }

            System.out.println("------------------------------");

            try {
                importPools(new File(parent + "/pools.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with pools.txt");
            }

            try {
                importPoolsConfig(new File(parent + "/poolconfig.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with poolconfig.txt");
            }

            try {
                importCredits(new File(parent + "/credits.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with credits.txt");
            }

            try {
                importMessages(new File(parent + "/messages.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("------------------------------");
                logger.error("Error with messages.txt");
            }

            System.out.println("------------------------------");

            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
            ses.scheduleAtFixedRate(com.marsss.callerphone.Callerphone::kill, 0, 2, TimeUnit.MINUTES);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }

    }

    private static void kill() {
        writeData();
//        for (ConvoStorage c : TCCallerphone.convos) {
//
//            if (System.currentTimeMillis() - c.getLastMessage() >= 250000) {
//                final String callerID = c.getCallerTCID();
//                try {
//                    jda.getTextChannelById(callerID).sendMessage(Callerphone + "Took too long for someone to pick up :(").queue();
//                    c.resetMessage();
//                    continue;
//                } catch (Exception ex) {
//                }
//
//                final String receiverID = c.getReceiverTCID();
//
//                ArrayList<String> DATA = new ArrayList<>(c.getMessages());
//
//                c.resetMessage();
//                try {
//                    jda.getTextChannelById(callerID).sendMessage(Callerphone + "Call ended due to inactivity.").queue();
//                } catch (Exception ex) {
//                }
//                try {
//                    jda.getTextChannelById(receiverID).sendMessage(Callerphone + "Call ended due to inactivity.").queue();
//                } catch (Exception ex) {
//                }
//
//                if (c.getReport()) {
//
//                    LocalDateTime now = LocalDateTime.now();
//                    final String month = String.valueOf(now.getMonthValue());
//                    final String day = String.valueOf(now.getDayOfMonth());
//                    final String hour = String.valueOf(now.getHour());
//                    final String minute = String.valueOf(now.getMinute());
//                    final String ID = month + "/" + day + "/" + hour + "/" + minute + "C" + callerID + "R" + receiverID;
//
//                    StringBuilder data = new StringBuilder();
//                    for (String m : DATA)
//                        data.append(m).append("\n");
//                    jda.getTextChannelById(reportchannel).sendMessage("**ID:** " + ID).addFile(data.toString().getBytes(), ID + ".txt").queue();
//
//                }
//
//            }
//        }
    }

    private static void writeData() {
        try {
            exportPools();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            exportMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            exportCredits();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void importMessages(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split(":");
                String user = split[0];
                String[] messages = split[1].split(",");

                userExecuted.put(user, Long.valueOf(messages[0]));
                userTransmitted.put(user, Long.valueOf(messages[1]));

                line = br.readLine();
            }
        }
    }
    private static void exportMessages() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        try (PrintWriter myWriter = new PrintWriter(parent + "/messages.txt")) {
            for (Map.Entry<String, Long> user : userExecuted.entrySet()) {
                sb.append(user.getKey()).append(":");
                sb.append(userExecuted.get(user.getKey()))
                        .append(",")
                        .append(userTransmitted.get(user.getKey()));
                sb.append("\n");
            }

            myWriter.print(sb);
            myWriter.close();
        }
    }

    private static void importCredits(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split(":");
                String user = split[0];
                String credits = split[1];

                userCredits.put(user, Long.valueOf(credits));

                line = br.readLine();
            }
        }
    }
    private static void exportCredits() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        try (PrintWriter myWriter = new PrintWriter(parent + "/credits.txt")) {
            for (Map.Entry<String, Long> user : userCredits.entrySet()) {
                sb.append(user.getKey()).append(":");
                sb.append(userCredits.get(user.getKey()));
                sb.append("\n");
            }

            myWriter.print(sb);
            myWriter.close();
        }
    }

    private static void getInfo(File file) throws IOException, InterruptedException {
        System.out.println("\nInfo.txt:");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();

        while (line != null) {
            if (line.startsWith("brainurl=")) {
                brainURL = line.substring(9).trim();
            } else if (line.startsWith("emoji=")) {
                Callerphone = line.substring(6).trim() + " ";
            } else if (line.startsWith("log=")) {
                logstatus = line.substring(4).trim();
            } else if (line.startsWith("report=")) {
                reportchannel = line.substring(7).trim();
            } else if (line.startsWith("invite=")) {
                invite = line.substring(7).trim();
            } else if (line.startsWith("support=")) {
                support = line.substring(8).trim();
            } else if (line.startsWith("tunessupport=")) {
                tunessupport = line.substring(13).trim();
            } else if (line.startsWith("donate=")) {
                donate = line.substring(7).trim();
            } else if (line.startsWith("owner=")) {
                owner = line.substring(6).trim();
            } else {
                System.out.println("Unused value:");
            }

            System.out.println(line);

            line = br.readLine();
        }
        br.close();

        jda.awaitReady();

        System.out.println("Log Status Channel: " + jda.getTextChannelById(logstatus).getAsMention());
        System.out.println("Report Channel: " + jda.getTextChannelById(reportchannel).getAsMention());
        if (isQuickStart) {
            System.out.println("Owner: " + owner + " (unable to obtain tag because of quickstart)");
        } else {
            System.out.println("Owner: " + jda.getUserById(owner).getAsTag());
        }

        System.out.println();
    }

    private static void getAdmin(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while (line != null) {
                admin.add(line);
                line = br.readLine();
            }
        }
    }

    private static void getPrefix(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                int split = line.indexOf("|");
                prefix.put(line.substring(0, split), line.substring(split + 1));
                line = br.readLine();
            }
        }
    }

    private static void getBlack(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while (line != null) {
                blacklist.add(line);
                line = br.readLine();
            }
        }
    }

    private static void getFilter(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while (line != null) {
                filter.add(line);
                line = br.readLine();
            }
        }
    }

    public static void run() throws InterruptedException {
        commandPrompt();
    }

    private static void commandPrompt() throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.println("{}Command Line Loaded...{}\nWelcome to Callerphone Bot Command Line (CBCL)!");

        while (true) {
            String cmd = sc.nextLine();
            if (cmd.startsWith("start")) {
                System.out.println("Token: ");
                String TOKEN = sc.nextLine();
                logger.info("Starting Bot...");
                if (jda != null) {
                    logger.info("Bot Is Online Right Now");
                } else {
                    isQuickStart = false;
                    BotInit(TOKEN, cmd.replaceFirst("start", ""), false);
                }
                continue;
            }

            if (cmd.startsWith("quickstart")) {
                System.out.println("Token: ");
                String TOKEN = sc.nextLine();
                logger.info("Starting Bot...");
                if (jda != null) {
                    logger.info("Bot Is Online Right Now");
                } else {
                    isQuickStart = true;
                    BotInit(TOKEN, cmd.replaceFirst("quickstart", ""), true);
                }
                continue;
            }

            if (cmd.equals("shutdown")) {
                logger.info("Shutting Down Bot...");
                if (jda != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status").setColor(new Color(213, 0, 0)).setFooter("Goodbye World...").setDescription(jda.getSelfUser().getAsMention() + " is going offline;" + cmd.replaceFirst("shutdown", ""));
                    try {
                        jda.getTextChannelById(logstatus).sendMessageEmbeds(embedBuilder.build()).complete();
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Error Sending Shutdown Message");
                    }
                    jda.awaitReady();
                    jda.shutdown();
                    jda = null;
                }
                logger.info("Bot Offline");
                sc.close();
                System.exit(0);
            }

            if (cmd.equals("presence")) {
                if (jda == null) {
                    logger.info("Bot Is Offline");
                    //continue;
                }

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
                    if (jda != null) {
                        jda.getPresence().setPresence(s, act);
                        continue;
                    }

                    logger.info("Bot Is Offline");
                    continue;

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Input error, please try again");
                    break;
                }

            }

            if (cmd.equals("info")) {
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
                continue;
            }

            if (cmd.equals("recal")) {
                logger.info("Recalibrating...");
                try {
                    getInfo(new File(parent + "/info.txt"));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with info.txt");
                    logger.warn("Critical Issues May Appear (BrainURL and other links)");
                }

                try {
                    getBlack(new File(parent + "/blacklist.txt"));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with blacklist.txt");
                }

                try {
                    getPrefix(new File(parent + "/prefix.txt"));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with prefix.txt");
                }

                try {
                    getAdmin(new File(parent + "/admin.txt"));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with admin.txt");
                }

                try {
                    getFilter(new File(parent + "/filter.txt"));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with filter.txt");
                }

                System.out.println("------------------------------");
            }

            if (cmd.equals("poolnum")) {
                System.out.println("Currently there are " + ChannelPool.config.size() + " channel pools running.");
                continue;
            }

            if (cmd.equals("exportpools")) {
                try {
                    exportPools();
                    logger.info("Successful export");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with pools.txt");
                    logger.warn("Some or no pools may be exported");
                }
                continue;
            }

            if (cmd.equals("importpools")) {
                try {
                    importPools(new File(parent + "/pools.txt"));
                    logger.info("Successful import");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with pools.txt");
                    logger.warn("Some or no pools may be imported");
                }
                continue;
            }

            if (cmd.equals("importconfig")) {
                try {
                    importPoolsConfig(new File(parent + "/poolconfig.txt"));
                    logger.info("Successful import");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------------------------------");
                    logger.error("Error with pools.txt");
                    logger.warn("Some or no pool config may be imported");
                }
                continue;
            }

            if (cmd.equals("updateCMD")) {
                update();
                System.out.println("Done Updating");
                continue;
            }

            if (cmd.equals("upsertCMD")) {
                upsert();
                System.out.println("Done Upserting");
                continue;
            }

            if (cmd.equals("help")) {
                System.out.println(
                        "Option  1: start <msg> = To start the bot\n" +
                                "Option  2: shutdown = To shutdown the bot\n" +
                                "Option  3: presence = To set presence\n" +
                                "Option  4: info = To get info of the bot\n" +
                                "Option  5: recal = To read resources again\n" +
                                "Option  6: poolnum = To see number of running pools\n" +
                                "Option  7: exportpools = Export pools to pools.txt and poolconfig.txt\n" +
                                "Option  8: importpools = Import pools from pools.txt\n" +
                                "Option  9: importconfig = Import pools config from poolconfig.txt\n" +
                                "Option 10: updateCMD = Remove all slash commands\n" +
                                "Option 11: upsertCMD = Upsert all slash commands\n" +
                                "Option 12: help = UBCL help (this)\n\n" +
                                "Other: quickstart <msg> = To start the bot quicker");
                continue;
            }

            if (!cmd.equals(""))
                logger.warn("Unknown Command");

        }
    }

    public static void award(User user, int amount) {
        userCredits.put(user.getId(), userCredits.getOrDefault(user.getId(), 0L) + amount);
        logger.info("User: " + user.getId() + " earned: " + amount + " credits.");
    }

    public static long getCredits(User user) {
        return userCredits.getOrDefault(user.getId(), 0L);
    }

    public static void addExecute(User user, int amount) {
        userExecuted.put(user.getId(), userExecuted.getOrDefault(user.getId(), 0L) + amount);

        userTransmitted.put(user.getId(), userTransmitted.getOrDefault(user.getId(), 0L));
    }

    public static long getExecuted(User user) {
        return userExecuted.getOrDefault(user.getId(), 0L);
    }

    public static void addTransmit(User user, int amount) {
        userTransmitted.put(user.getId(), userTransmitted.getOrDefault(user.getId(), 0L) + amount);

        userExecuted.put(user.getId(), userExecuted.getOrDefault(user.getId(), 0L));
    }

    public static long getTransmitted(User user) {
        return userTransmitted.getOrDefault(user.getId(), 0L);
    }

    private static void update() {
        jda.updateCommands().queue();
    }
    private static void upsert() {
        jda.upsertCommand(new CommandData("about", "About Callerphone")).queue();
        jda.upsertCommand(new CommandData("donate", "Help us out by donating")).queue();
        jda.upsertCommand(new CommandData("invite", "Invite Callerphone")).queue();
        jda.upsertCommand(new CommandData("ping", "Get the bot's ping")).queue();
        jda.upsertCommand(new CommandData("uptime", "Get the bot's uptime")).queue();

        jda.upsertCommand(new CommandData("chat", "Chat with people from other servers")
                        .addSubcommands(
                                new SubcommandData("default", "Chat with people from other servers"),
                                new SubcommandData("anonymous", "Chat anonymously"),
                                new SubcommandData("familyfriendly", "Chat with swear word censoring"),
                                new SubcommandData("ffandanon", "Chat family friendly and anonymously")))
                .queue();
        jda.upsertCommand(new CommandData("endchat", "End chatting with people from another server")).queue();
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

    private static void exportPools() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        try (PrintWriter myWriter = new PrintWriter(parent + "/pools.txt")) {
            for (Map.Entry<String, ArrayList<String>> pool : ChannelPool.childr.entrySet()) {
                sb.append(pool.getKey()).append(":");
                for (String id : pool.getValue()) {
                    sb.append(id).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
            }

            myWriter.print(sb);
            myWriter.close();
        }
        StringBuilder sb2 = new StringBuilder();
        try (PrintWriter myWriter = new PrintWriter(parent + "/poolconfig.txt")) {
            for (Map.Entry<String, PoolConfig> pool : ChannelPool.config.entrySet()) {
                sb2.append(pool.getKey()).append(":");
                PoolConfig config = pool.getValue();
                sb2.append(config.getPwd()).append(",").append(config.getCap()).append(",").append(config.isPub());
                sb2.append("\n");
            }

            myWriter.print(sb2);
            myWriter.close();
        }
    }

    private static void importPoolsConfig(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split(":");
                String host = split[0];
                String[] config = split[1].split(",");
                ChannelPool.config.put(host, new PoolConfig(config[0], Integer.parseInt(config[1]), Boolean.parseBoolean(config[2])));

                line = br.readLine();
            }
        }
    }

    private static void importPools(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split(":");
                String host = split[0];
                String[] children = split[1].split(",");
                ChannelPool.hostPool(host);
                for (String id : children) {
                    ChannelPool.joinPool(host, id, "");
                }
                line = br.readLine();
            }
        }
    }
}
