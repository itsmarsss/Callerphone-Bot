package com.itsmarsss.callerphone;

import java.awt.Color;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itsmarsss.ICommand;
import com.itsmarsss.callerphone.minigames.commands.PlayMiniGame;
import com.itsmarsss.callerphone.minigames.commands.ShowMiniGames;
import com.itsmarsss.callerphone.minigames.handlers.BattleShipHandler;
import com.itsmarsss.callerphone.minigames.handlers.Connect4Handler;
import com.itsmarsss.callerphone.minigames.handlers.TicTacToeHandler;
import com.itsmarsss.callerphone.minigames.handlers.WordSearchHandler;
import com.itsmarsss.callerphone.msginbottle.commands.FindBottle;
import com.itsmarsss.callerphone.msginbottle.commands.SendBottle;
import com.itsmarsss.callerphone.msginbottle.commands.ViewBottle;
import com.itsmarsss.callerphone.msginbottle.handlers.*;
import com.itsmarsss.callerphone.msginbottle.modals.SendModal;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.discord.match.MatchButtonHandler;
import com.itsmarsss.callerphone.discord.match.MatchCommand;
import com.itsmarsss.callerphone.discord.match.MatchDmListener;
import com.itsmarsss.callerphone.discord.match.MatchModalHandler;
import com.itsmarsss.callerphone.discord.mod.ModCommandRouter;
import com.itsmarsss.callerphone.call.discord.CallButtonHandler;
import com.itsmarsss.callerphone.call.discord.CallCommand;
import com.itsmarsss.callerphone.call.discord.CallListener;
import com.itsmarsss.callerphone.call.discord.EndCallCommand;
import com.itsmarsss.callerphone.call.discord.PrefixCommand;
import com.itsmarsss.callerphone.call.discord.ReportCallCommand;
import com.itsmarsss.callerphone.call.service.CallSessionService;
import com.itsmarsss.callerphone.users.commands.DeductCredits;
import com.itsmarsss.callerphone.users.commands.Leaderboard;
import com.itsmarsss.callerphone.users.commands.Profile;
import com.itsmarsss.callerphone.users.commands.RewardCredits;
import com.itsmarsss.commandType.IButtonInteraction;
import com.itsmarsss.commandType.IModalInteraction;
import com.itsmarsss.commandType.IStringSelectInteraction;
import com.itsmarsss.database.MongoConnector;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itsmarsss.callerphone.bot.*;
import com.itsmarsss.callerphone.listeners.*;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Callerphone {

    public static final Logger logger = LoggerFactory.getLogger(Callerphone.class);

    public static String parent;


    public static final List<ICommand> cmdLst = new ArrayList<>();
    public static final Map<String, ICommand> cmdMap = new ConcurrentHashMap<>();
    public static final Map<String, IModalInteraction> mdlMap = new ConcurrentHashMap<>();
    public static final Map<String, IButtonInteraction> btnMap = new ConcurrentHashMap<>();
    public static final Map<String, IStringSelectInteraction> selMap = new ConcurrentHashMap<>();

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
            GatewayIntent.GUILD_EXPRESSIONS,
            GatewayIntent.GUILD_INVITES,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT);

    public static final String VERSION = "6.0.0";

    public static void run() throws InterruptedException, URISyntaxException, UnsupportedEncodingException {
        ToolSet.printWelcome();

        parent = URLDecoder.decode(
                new File(Callerphone.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .getParentFile().getPath(),
                "UTF-8");

        logger.info("Parent directory: {}", parent);

        if (!readConfigYML()) {
            logger.error("Invalid config.yml — require botToken and databaseURL (and ensure the file exists)");
            System.exit(1);
        }

        if (!dbConnector.init()) {
            logger.error("Cannot connect to MongoDB — check databaseURL in config.yml (mongodb://host:port)");
            System.exit(1);
        }

        ApplicationContext.init(dbConnector.getMongoDatabase(), config.getMatchMediaChannel());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (ApplicationContext.isReady()) {
                    ApplicationContext.get().shutdown();
                }
                dbConnector.close();
            } catch (Exception e) {
                logger.warn("Shutdown cleanup failed", e);
            }
        }, "callerphone-shutdown"));

        ToolSet.updateToolSet();
        new CommandPrompt().startPrompting();
    }

    private static boolean readConfigYML() {
        try (InputStream is = Files.newInputStream(Paths.get(parent + "/config.yml"))) {
            Yaml yml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
            config = yml.load(is);
            return config != null && config.isValid();
        } catch (Exception e) {
            logger.error("Failed to read config.yml", e);
            return false;
        }
    }

    public static void botInit(String token, String startupmsg) {
        try {
            if (isQuickStart) {
                sdMgr = DefaultShardManagerBuilder.createDefault(token, intent)
                        .setShardsTotal(-1)
                        .build();
            } else {
                sdMgr = DefaultShardManagerBuilder.createDefault(token, intent)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .setShardsTotal(-1)
                        .build();
            }

            selfUser = sdMgr.getShards().get(0).getSelfUser();

            logger.info("Shard count: {}", sdMgr.getShardsTotal());
            registerCommands();
            registerModals();
            registerButtons();
            registerSelects();
            registerListeners();

            sdMgr.setActivity(Activity.watching("for /help"));
            logger.info("Bot online with {} guilds", sdMgr.getGuilds().size());

            final TextChannel logChannel = ToolSet.getTextChannel(config.getLogStatusChannel());
            if (logChannel == null) {
                logger.error("Invalid log status channel; startup message not sent");
            } else {
                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status")
                        .setColor(new Color(24, 116, 52))
                        .setFooter("Hello World!")
                        .setDescription(sdMgr.getShards().get(0).getSelfUser().getAsMention() + " is now online;" + startupmsg);
                logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        } catch (Exception e) {
            logger.error("Bot init failed", e);
        }
    }

    private static void registerCommands() {
        ICommand[] commands = {
                new About(), new BotInfo(), new Donate(), new Help(), new Invite(),
                new Profile(), new Leaderboard(),
                new CallCommand(), new EndCallCommand(), new ReportCallCommand(), new PrefixCommand(),
                new MatchCommand(),
                new DeductCredits(), new RewardCredits(),
                new PlayMiniGame(), new ShowMiniGames(),
                new com.itsmarsss.callerphone.msginbottle.commands.BottleCommand(),
                new FindBottle(), new SendBottle(), new ViewBottle()
        };

        for (ICommand cmd : commands) {
            cmdLst.add(cmd);
            cmdMap.put(cmd.getName(), cmd);
            logger.debug("Registered command: {}", cmd.getName());
        }
        logger.info("Registered {} commands", cmdLst.size());
    }

    private static void registerModals() {
        IModalInteraction[] modals = {new SendModal(), new MatchModalHandler()};
        for (IModalInteraction modal : modals) {
            mdlMap.put(modal.getID(), modal);
            logger.debug("Registered modal: {}", modal.getID());
        }
        logger.info("Registered {} modals", mdlMap.size());
    }

    private static void registerButtons() {
        IButtonInteraction[] buttons = {
                new TicTacToeHandler(), new Connect4Handler(), new BattleShipHandler(),
                new WordSearchHandler(), new AddPageHandler(), new NextHandler(),
                new PreviousHandler(), new ReportHandler(), new SaveHandler(),
                new CallButtonHandler(), new MatchButtonHandler(),
                new com.itsmarsss.callerphone.bot.HelpButtonHandler()
        };
        for (IButtonInteraction button : buttons) {
            btnMap.put(button.getID(), button);
            logger.debug("Registered button: {}", button.getID());
        }
        logger.info("Registered {} buttons", btnMap.size());
    }

    private static void registerSelects() {
        IStringSelectInteraction[] selects = {
                new com.itsmarsss.callerphone.discord.match.MatchSelectHandler(),
                new com.itsmarsss.callerphone.call.discord.CallSelectHandler(),
                new com.itsmarsss.callerphone.msginbottle.BottleSelectHandler()
        };
        for (IStringSelectInteraction select : selects) {
            selMap.put(select.getID(), select);
            logger.debug("Registered select: {}", select.getID());
        }
        logger.info("Registered {} select handlers", selMap.size());
    }

    private static void registerListeners() {
        sdMgr.addEventListener(
                new OnButtonClick(),
                new OnStringSelect(),
                new ModCommandRouter(),
                new MatchDmListener(),
                new OnModalEvent(),
                new OnOtherEvent(),
                new OnSlashCommand(),
                new CallListener()
        );
        CallSessionService.get();
    }
}
