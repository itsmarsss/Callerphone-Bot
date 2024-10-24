package com.marsss.callerphone;

import java.awt.Color;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.marsss.ICommand;
import com.marsss.callerphone.channelpool.commands.*;
import com.marsss.callerphone.channelpool.modals.SettingsModal;
import com.marsss.callerphone.minigames.IMiniGame;
import com.marsss.callerphone.minigames.commands.PlayMiniGame;
import com.marsss.callerphone.minigames.games.BattleShip;
import com.marsss.callerphone.minigames.games.Connect4;
import com.marsss.callerphone.minigames.games.TicTacToe;
import com.marsss.callerphone.minigames.handlers.TicTacToeHandler;
import com.marsss.callerphone.msginbottle.commands.FindBottle;
import com.marsss.callerphone.msginbottle.commands.SendBottle;
import com.marsss.callerphone.msginbottle.handlers.ReportHandler;
import com.marsss.callerphone.msginbottle.handlers.SaveHandler;
import com.marsss.callerphone.msginbottle.modals.SendModal;
import com.marsss.callerphone.users.commands.DeductCredits;
import com.marsss.callerphone.users.commands.Profile;
import com.marsss.callerphone.users.commands.RewardCredits;
import com.marsss.callerphone.tccallerphone.TCCallerphone;
import com.marsss.callerphone.tccallerphone.TCCallerphoneListener;
import com.marsss.callerphone.tccallerphone.commands.*;
import com.marsss.commandType.IButtonInteraction;
import com.marsss.commandType.IModalInteraction;
import com.marsss.database.MongoConnector;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    public static HashMap<String, String> replyMap = new HashMap<>();

    public static final ArrayList<ICommand> cmdLst = new ArrayList<>();
    public static final HashMap<String, ICommand> cmdMap = new HashMap<>();
    public static final HashMap<String, IModalInteraction> mdlMap = new HashMap<>();
    public static final HashMap<String, IButtonInteraction> btnMap = new HashMap<>();

    public static boolean isQuickStart;

    public static ShardManager sdMgr;
    public static User selfUser;

    public static Config config = new Config();
    public static MongoConnector dbConnector = new MongoConnector();

    private static final EnumSet<GatewayIntent> intent = EnumSet.of(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.GUILD_INVITES,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT);

    public final String version = "5.0.0";

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

        if (!dbConnector.init()) {
            System.out.println("______________________________________________________");
            System.out.println("Cannot connect to MongoDB via URL");
            System.out.println("\t1. Make sure databaseURL exists in config.yml");
            System.out.println("\t2. Make sure databaseURL follows the format: \"mongodb://ip:port\"");
            System.exit(0);
        }

        ToolSet.updateToolSet();

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

    public static void botInit(String token, String startupmsg) {
        try {
            if (isQuickStart) {
                sdMgr = DefaultShardManagerBuilder.createDefault(token, intent)
                        .enableCache(CacheFlag.VOICE_STATE)
                        .enableCache(CacheFlag.ROLE_TAGS)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build();
            } else {
                sdMgr = DefaultShardManagerBuilder.createDefault(token, intent)
                        .enableCache(CacheFlag.VOICE_STATE)
                        .enableCache(CacheFlag.ROLE_TAGS)
                        .setChunkingFilter(ChunkingFilter.ALL)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .setShardsTotal(-1)
                        .build();
            }
            //builder.setShards(shardId, shardTotal); // Set the shard ID and total number of shards for this instance
            //builder.addEventListeners(/* Add event listeners */);

            selfUser = sdMgr.getShards().get(0).getSelfUser();


            System.out.println("Shard Count: " + sdMgr.getShardsTotal());

            System.out.println("Mapping Commands:");

            cmdLst.add(new About());
            cmdLst.add(new BotInfo());
            cmdLst.add(new Donate());
            cmdLst.add(new Invite());
            cmdLst.add(new Profile());


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
            cmdLst.add(new PoolSettings());
            cmdLst.add(new KickPool());

            cmdLst.add(new DeductCredits());
            cmdLst.add(new RewardCredits());

            cmdLst.add(new PlayMiniGame());

            cmdLst.add(new FindBottle());
            cmdLst.add(new SendBottle());

            for (ICommand cmd : cmdLst) {
                for (String trigger : cmd.getTriggers()) {
                    cmdMap.put(trigger, cmd);
                    System.out.println("Put: key=" + trigger + ", value=" + cmd.getClass().getName());
                }
            }


            System.out.println();
            System.out.println();
            System.out.println("Mapping Modals:");

            ArrayList<IModalInteraction> mdlLst = new ArrayList<>();
            mdlLst.add(new SendModal());
            mdlLst.add(new SettingsModal());

            for (IModalInteraction mdl : mdlLst) {
                mdlMap.put(mdl.getID(), mdl);
                System.out.println("Put: key=" + mdl.getID() + ", value=" + mdl.getClass().getName());
            }


            System.out.println();
            System.out.println();
            System.out.println("Mapping Buttons:");

            ArrayList<IButtonInteraction> btnLst = new ArrayList<>();
            btnLst.add(new TicTacToeHandler());
            btnLst.add(new ReportHandler());
            btnLst.add(new SaveHandler());

            for (IButtonInteraction btn : btnLst) {
                btnMap.put(btn.getID(), btn);
                System.out.println("Put: key=" + btn.getID() + ", value=" + btn.getClass().getName());
            }

            ArrayList<IMiniGame> gameLst = new ArrayList<>();

            gameLst.add(new BattleShip());
            gameLst.add(new TicTacToe());
            gameLst.add(new Connect4());

            sdMgr.addEventListener(new OnButtonClick());
            sdMgr.addEventListener(new OnMessage());
            sdMgr.addEventListener(new OnModalEvent());
            sdMgr.addEventListener(new OnOtherEvent());
            sdMgr.addEventListener(new OnSlashCommand());
            sdMgr.addEventListener(new TCCallerphoneListener());
            sdMgr.addEventListener(new ChannelPoolListener());


            for (int i = 0; i < 10000; i++) {
                TCCallerphone.convos.add(new ConvoStorage(new ConcurrentLinkedQueue<>(), "empty", "", 0, 0, true, true, false, false, false));
            }

            sdMgr.setActivity(Activity.watching("for " + config.getPrefix() + "help"));
            logger.info("Bot online");

            System.out.println("\nGuild List: ");
            for (Guild g : sdMgr.getGuilds()) {
                System.out.println("- " + g.getName());
            }

            final TextChannel LOG_CHANNEL = ToolSet.getTextChannel(config.getLogStatusChannel());
            if (LOG_CHANNEL == null) {
                System.out.println("------------------------------");
                logger.error("Error Sending Startup Message");
            } else {
                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status")
                        .setColor(new Color(24, 116, 52))
                        .setFooter("Hello World!")
                        .setDescription(sdMgr.getShards().get(0).getSelfUser().getAsMention() + " is now online;" + startupmsg);
                LOG_CHANNEL.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }
}
