package com.marsss.callerphone;

import java.awt.Color;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.marsss.callerphone.channelpool.commands.*;
import com.marsss.callerphone.users.commands.DeductCredits;
import com.marsss.callerphone.users.commands.RewardCredits;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import com.marsss.callerphone.tccallerphone.TCCallerphoneListener;
import com.marsss.callerphone.tccallerphone.commands.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marsss.ICommand;
import com.marsss.callerphone.bot.*;
import com.marsss.callerphone.utils.*;
import com.marsss.callerphone.listeners.*;
import com.marsss.callerphone.channelpool.*;
import com.marsss.callerphone.tccallerphone.ConvoStorage;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Callerphone {

    public static final Logger logger = LoggerFactory.getLogger(Callerphone.class);

    public static String parent;


    public static final HashMap<String, ICommand> cmdMap = new HashMap<>();

    public static boolean isQuickStart;

    public static JDA jda;

    public static Config config = new Config();
    public static Storage storage = new Storage();

    public static final String ERROR_MSG = "An error occurred with error: `%s`." +
            "\nIf this is a recurring problem, please join our support server and report this issue. " + config.getSupportServer();

    private static final EnumSet<GatewayIntent> intent = EnumSet.of(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_INVITES,
            GatewayIntent.DIRECT_MESSAGES);

    public static void run() throws InterruptedException, URISyntaxException, UnsupportedEncodingException {
        ToolSet.printWelcome();

        parent = URLDecoder.decode(new File(Callerphone.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath(), "UTF-8");

        System.out.println("\nParent - " + parent);

        if (!readConfigYML()) {
            System.out.println("______________________________________________________");
            System.out.println("There was an error with config.yml");
            System.out.println("\t1. Make sure config.yml template exists");
            System.out.println("\t2. Make sure config.yml values are correctly inputted");
            System.exit(0);
        }

        new CommandPrompt().startPrompting();
    }

    private static boolean readConfigYML() {
        InputStream is;
        try {
            is = Files.newInputStream(Paths.get(parent + "/config.yml"));
            Yaml yml = new Yaml(new Constructor(Config.class));
            config = (Config) yml.load(is);
            return config.isValid();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void BotInit(String token, String startupmsg, boolean quickStart) {

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
            //cmdLst.add(new ChannelInfo());
            cmdLst.add(new Colour());
            cmdLst.add(new Help());
            //cmdLst.add(new RoleInfo());
            //cmdLst.add(new Search());
            //cmdLst.add(new ServerInfo());
            //cmdLst.add(new UserInfo());

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

            cmdLst.add(new DeductCredits());
            cmdLst.add(new RewardCredits());

            for (ICommand cmd : cmdLst) {
                for (String trigger : cmd.getTriggers()) {
                    cmdMap.put(trigger, cmd);
                    System.out.println("Put: key=" + trigger + ", value=" + cmd.getClass().getName());
                }
            }

            jda.addEventListener(new CommandListener());
            jda.addEventListener(new OnOtherEvent());
            jda.addEventListener(new OnSlashCommand());
            //jda.addEventListener(new OnPrivateMessage());
            jda.addEventListener(new TCCallerphoneListener());
            jda.addEventListener(new ChannelPoolListener());


            for (int i = 0; i < 10000; i++) {
                TCCallerphone.convos.add(new ConvoStorage(new ConcurrentLinkedQueue<>(), "empty", "", 0, 0, true, true, false, false, false));
            }

            jda.awaitReady();

            jda.getPresence().setActivity(Activity.watching("for " + config.getPrefix() + "help"));
            logger.info("Bot online");

            System.out.println("\nGuild List: ");
            for (Guild g : jda.getGuilds()) {
                System.out.println("- " + g.getName());
            }

            try {
                Storage.readData();
            } catch (Exception e) {
                e.printStackTrace();
            }

            final TextChannel LOG_CHANNEL = ToolSet.getTextChannel(config.getLogStatusChannel());
            if (LOG_CHANNEL == null) {
                System.out.println("------------------------------");
                logger.error("Error Sending Startup Message");
            } else {
                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status").setColor(new Color(24, 116, 52)).setFooter("Hello World!").setDescription(jda.getSelfUser().getAsMention() + " is now online;" + startupmsg);
                LOG_CHANNEL.sendMessageEmbeds(embedBuilder.build()).queue();
            }

            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
            ses.scheduleAtFixedRate(com.marsss.callerphone.Callerphone::kill, 0, 2, TimeUnit.MINUTES);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }

    }

    private static void kill() {
        Storage.writeData();
        logger.info("All data exported.");
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
}
