package com.marsss;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marsss.listeners.*;
import com.marsss.tccallerphone.ConvoStorage;
import com.marsss.tccallerphone.ConvoStorage.Convo;
import com.marsss.vccallerphone.AudioStorage;
import com.marsss.vccallerphone.AudioStorage.Audio;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
//import net.dv8tion.jda.api.interactions.commands.OptionType;
//import net.dv8tion.jda.api.interactions.commands.build.CommandData;
//import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {

	public static final Logger logger = LoggerFactory.getLogger(Bot.class);

	public static String parent;

	public static LinkedList <String> blacklist = new LinkedList<>();
	public static HashMap <String, String> prefix = new HashMap<String, String>();
	public static LinkedList <String> admin = new LinkedList<>();

	public static LinkedList <String> filter = new LinkedList<>();

	public static final String Prefix = "c?";
	public static final String ThumbsUp = "👍";

	public static String brainURL = "http:api.brainshop.ai/get?bid=160403&key=FFFNOBQEMnANoVn1&uid=[uid]&msg=[msg]";
	public static String Callerphone = "<:CallerphoneEmote:899051549173637120>";
	public static String logstatus = "";
	public static String reportchannel = "";
	public static String invite = "https://discord.com/api/oauth2/authorize?client_id=849713468348956692&permissions=49663040&scope=bot%20applications.commands";
	public static String support = "https://discord.gg/jcYKsfw48p";
	public static String tunessupport = "https://discord.gg/TyHaxtWAmX";
	public static String owner = "841028865995964477";

	public static JDA jda;

	private static final EnumSet<GatewayIntent> intent = EnumSet.of(
			GatewayIntent.GUILD_MEMBERS,
			GatewayIntent.GUILD_MESSAGES,
			GatewayIntent.GUILD_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_VOICE_STATES,
			GatewayIntent.GUILD_EMOJIS,
			GatewayIntent.GUILD_INVITES,
			GatewayIntent.DIRECT_MESSAGES);

	private static void BotInit(String token, String startupmsg) throws InterruptedException, LoginException {

		try {
			jda = JDABuilder.createDefault(token, intent)
					.enableCache(CacheFlag.VOICE_STATE)
					.enableCache(CacheFlag.ROLE_TAGS)
					.setChunkingFilter(ChunkingFilter.ALL)
					.setMemberCachePolicy(MemberCachePolicy.ALL)
					.build();

			jda.addEventListener(new CommandListener());
			jda.addEventListener(new OnOtherEvent());
			jda.addEventListener(new OnSlashCommand());
			jda.addEventListener(new OnPrivateMessage());
			jda.addEventListener(new TCCallerphoneListener());
			jda.addEventListener(new VCCallerphoneListener());
			jda.addEventListener(new OnMuted());
			jda.addEventListener(new OnDeafened());
			jda.addEventListener(new OnDisconnection());



			for(int i = 0; i < ConvoStorage.convo.length; i++) {
				ConvoStorage.convo[i] = new Convo(new ConcurrentLinkedQueue<>(), "empty", "", false, 0, true, true, false, false, false);
			}


			for(int i = 0; i < AudioStorage.audio.length; i++) {
				AudioStorage.audio[i] = new Audio(new ConcurrentLinkedQueue<>(), "empty", "", new ConcurrentLinkedQueue<>(), "", "", false, false, false);
			}

			jda.awaitReady();

			jda.getPresence().setActivity(Activity.watching("for " + Prefix + "help | have fun"));
			logger.info("Bot online");

			System.out.println("\nGuild List: ");
			for(Guild g : jda.getGuilds()) {
				System.out.println("- " + g.getName());
			}


			parent = URLDecoder.decode(new File(Bot.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath(), "UTF-8");
			System.out.println("\nParent - " + parent);


			try {
				getInfo(new File(parent + "/info.txt"));
			}catch(Exception e) {
				logger.error("Error with info.txt, cannot continue startup");
				//jda.shutdownNow();
				//jda = null;
				logger.info("Bot offline");
				//return;
			}
			
			try {
				EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status").setColor(Color.GREEN).setFooter("Hello World!").setDescription(jda.getSelfUser().getAsMention() + " is now online;" + startupmsg);
				jda.getTextChannelById(logstatus).sendMessageEmbeds(embedBuilder.build()).queue();
			} catch(Exception e) {
				logger.error("Error Sending Startup Message");
			}

			try {
				getBlack(new File(parent + "/blacklist.txt"));
			}catch(Exception e) {
				logger.error("Error with blacklist.txt");
			}

			try {
				getPrefix(new File(parent + "/prefix.txt"));
			}catch(Exception e) {
				logger.error("Error with prefix.txt");
			}

			try{
				getAdmin(new File(parent + "/admin.txt"));
			}catch(Exception e) {
				logger.error("Error with admin.txt");
			}

			try{
				getFilter(new File(parent + "/filter.txt"));
			}catch(Exception e) {
				logger.error("Error with filter.txt");
			}

			ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
			ses.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					kill();
				}
			}, 0, 2, TimeUnit.MINUTES);

		}catch(Exception e) {
			logger.error(e.toString());
		}

		/*		jda.upsertCommand(new CommandData("help", "Learn more about my commands")
						.addOptions(new OptionData(OptionType.STRING, "command", "Which command/category you want to learn more about?").setRequired(false)))
				.queue();
		
				jda.upsertCommand(new CommandData("botinfo", "Get the bot's info")).queue();
		
				jda.upsertCommand(new CommandData("search", "Search the web")
						.addOptions(new OptionData(OptionType.STRING, "query", "What do you want to search?").setRequired(true)))
				.queue();
		
				jda.upsertCommand(new CommandData("poll", "Creates a poll for members to vote")
						.addOptions(new OptionData(OptionType.STRING, "question", "What are we voting for?").setRequired(true)))
				.queue();
		
				
				jda.upsertCommand(new CommandData("about", "About Callerphone")).queue();
				jda.upsertCommand(new CommandData("donate", "Help us out by donating")).queue();
				jda.upsertCommand(new CommandData("ping", "Get the bot's ping")).queue();
				jda.upsertCommand(new CommandData("invite", "Invite Callerphone")).queue();
				jda.upsertCommand(new CommandData("ping", "Get the bot's ping")).queue();
				jda.upsertCommand(new CommandData("uptime", "Get the bot's uptime")).queue();
		
		
		
				jda.upsertCommand(new CommandData("clap", "Claps your message")
						.addOptions(new OptionData(OptionType.STRING, "message", "What do you want to clap?").setRequired(true)))
						.queue();
				
				jda.upsertCommand(new CommandData("color", "Get a random color")).queue();
		
				jda.upsertCommand(new CommandData("colorrgb", "COLORS!")
						.addOptions(new OptionData(OptionType.INTEGER, "r", "Red value").setRequired(true))
						.addOptions(new OptionData(OptionType.INTEGER, "g", "Green value").setRequired(true))
						.addOptions(new OptionData(OptionType.INTEGER, "b", "Blue value").setRequired(true)))
						.queue();
		
				jda.upsertCommand(new CommandData("colorhex", "COLORS!")
						.addOptions(new OptionData(OptionType.STRING, "hex", "Hexcode").setRequired(true)))
						.queue();
		
				jda.upsertCommand(new CommandData("echo", "Echos your message")
						.addOptions(new OptionData(OptionType.STRING, "message", "What do you want to echo?").setRequired(true)))
						.queue();
		
				jda.upsertCommand(new CommandData("eightball", "Help you decide things")
						.addOptions(new OptionData(OptionType.STRING, "question", "What do you want to ask?").setRequired(true)))
						.queue();
		*/

	}


	private static void getInfo(File file) throws IOException, InterruptedException {
		System.out.println("\nInfo.txt:");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();

		while (line != null) {
			if(line.startsWith("brainurl=")) {
				brainURL = line.substring(9);
			}else if(line.startsWith("emoji=")) {
				Callerphone = line.substring(6);
			}else if(line.startsWith("log=")) {
				logstatus = line.substring(4);
			}else if(line.startsWith("report=")) {
				reportchannel = line.substring(7);
			}else if(line.startsWith("invite=")) {
				invite = line.substring(7);
			}else if(line.startsWith("support=")) {
				support = line.substring(8);
			}else if(line.startsWith("tunessupport=")) {
				tunessupport = line.substring(13);
			}else if(line.startsWith("owner=")) {
				owner = line.substring(6);
			}else {
				System.out.println("Unused value:");
			}

			System.out.println(line);
			
			line = br.readLine();
		}
		br.close();

		jda.awaitReady();
		
		System.out.println(jda.getTextChannelById(logstatus).getAsMention());
		System.out.println(jda.getTextChannelById(reportchannel).getAsMention());
		System.out.println(jda.getUserById(owner).getAsTag());
		
		System.out.println();
	}

	private static void kill() {
		for(Convo c : ConvoStorage.convo) {

			if(System.currentTimeMillis()-c.lastMessage >= 250000) {
				final String callerID = c.getCallerTCID();
				if(!c.isConnected) {
					try {
						Bot.jda.getTextChannelById(callerID).sendMessage(Bot.Callerphone + "Took too long for someone to pick up :(").queue();
					}catch(Exception ex) {}
					c.resetMessage();
					continue;
				}
				final String receiverID = c.getReceiverTCID();

				String data = "";
				for(String m : c.getMessages())
					data += m + "\n";

				c.resetMessage();
				try {
					Bot.jda.getTextChannelById(callerID).sendMessage(Bot.Callerphone + "Call ended due to inactivity.").queue();
				}catch(Exception ex) {}
				try {
					Bot.jda.getTextChannelById(receiverID).sendMessage(Bot.Callerphone + "Call ended due to inactivity.").queue();
				}catch(Exception ex) {}
				LocalDateTime now = LocalDateTime.now();
				String month = String.valueOf(now.getMonthValue());
				String day = String.valueOf(now.getDayOfMonth());
				String hour = String.valueOf(now.getHour());
				String minute = String.valueOf(now.getMinute());
				String ID = month + day + hour + minute + callerID + receiverID;			

				final String DATA = data;
				if(c.report) {
					Bot.jda.getTextChannelById(reportchannel).sendMessage("**ID:** " + ID).addFile(DATA.getBytes(), ID + ".txt").queue();
				}
			}
		}
	}

	private static void getAdmin(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line = br.readLine();

			while (line != null) {
				admin.add(line);
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}
	private static void getPrefix(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line = br.readLine();
			while (line != null) {
				int split = line.indexOf("|");
				prefix.put(line.substring(0, split), line.substring(split+1, line.length()));
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}
	private static void getBlack(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line = br.readLine();

			while (line != null) {
				blacklist.add(line);
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}
	private static void getFilter(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line = br.readLine();

			while (line != null) {
				filter.add(line);
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}
	public static void main(String[] args) throws LoginException, InterruptedException {
		commandPrompt();
	}

	private static void commandPrompt() throws LoginException, InterruptedException {
		//Scanner sc = new Scanner(System.in);
		BotInit("ODQ5NzEzNDY4MzQ4OTU2Njky.YLfLVQ.hEvy6QsCFm0TsmsBgJRuBvguMzs", "start");
		logger.info("\n{}Command Line Loaded...{}\nWelcome to Callerphone Bot Command Line (CBCL)!");

//		while(true) {
//			String cmd = sc.nextLine();
//
//			if(cmd.startsWith("start")) {
//				System.out.println("Token: ");
//				String TOKEN = sc.nextLine();
//				logger.info("Starting Bot...");
//				if(jda != null) {
//					logger.info("Bot Is Online Right Now");
//				} else {
//					BotInit(TOKEN, cmd.replaceFirst("start", ""));
//				}
//				continue;
//			}
//
//			if(cmd.equals("shutdown")) {
//				logger.info("Shutting Down Bot...");
//				if(jda != null) {
//					EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status").setColor(Color.RED).setFooter("Goodbye World...").setDescription(Bot.jda.getSelfUser().getAsMention() + " is going offline;" + cmd.replaceFirst("shutdown", ""));
//					try {
//						jda.getTextChannelById(logstatus).sendMessageEmbeds(embedBuilder.build()).complete();
//					} catch(Exception e) {
//						logger.error("Error Sending Shutdown Message");
//					}
//					jda.awaitReady();
//					jda.shutdown();
//					jda = null;
//				}
//				logger.info("Bot Offline");
//				sc.close();
//				System.exit(0);
//			}
//
//			if(cmd.equals("presence")) {
//				if(jda == null) {
//					logger.info("Bot Is Offline");
//					//continue;
//				}
//
//				Activity act = null;
//				logger.info("Change Presence...");
//				try {
//					while(true) {
//
//						System.out.println("Activity: ");
//						String msg = sc.next().toLowerCase();
//						
//						if(msg.equals("<removestatus>")) {
//							act = null;
//							break;
//						} else if(msg.equals("competing")) {
//							System.out.println("Status Message: ");
//							sc.nextLine();
//							String comp = sc.nextLine();
//							System.out.println("Competing: " + comp);
//							act = Activity.competing(comp);
//							break;
//
//						} else if(msg.equals("listening")) {
//							System.out.println("Status Message: ");
//							sc.nextLine();
//							String song = sc.nextLine();
//							System.out.println("Listening: " + song);
//							act = Activity.listening(song);
//							break;
//
//						} else if(msg.equals("playing")) {
//							System.out.println("Status Message: ");
//							sc.nextLine();
//							String game = sc.nextLine();
//							System.out.println("Playing: " + game);
//							act = Activity.playing(game);
//							break;
//
//						} else if(msg.equals("streaming")) {
//							System.out.println("Title Message: ");
//							sc.nextLine();
//							String title = sc.nextLine();
//							System.out.println("Stream Link: ");
//							String link = sc.nextLine();
//							System.out.println("Title: " + title + "\n" + "Link: " + link);
//							act = Activity.streaming(title, link);
//							break;
//
//						} else if(msg.equals("watching")) {
//							System.out.println("Status Message: ");
//							sc.nextLine();
//							String watch = sc.nextLine();
//							System.out.println("Watching: " + watch);
//							act = Activity.watching(watch);
//							break;
//						}
//					}
//
//					OnlineStatus s = null;
//
//					while(true) {
//						System.out.println("Online Status: ");
//						String msg = sc.next().toLowerCase();
//
//						if(msg.toLowerCase().startsWith("onl")) {
//							s = OnlineStatus.ONLINE;
//							break;
//
//						} else if(msg.toLowerCase().startsWith("idl")) {
//							s = OnlineStatus.IDLE;
//							break;
//
//						} else if(msg.toLowerCase().startsWith("dnd")) {
//							s = OnlineStatus.DO_NOT_DISTURB;
//							break;
//
//						} else if(msg.toLowerCase().startsWith("inv")) {
//							s = OnlineStatus.INVISIBLE;
//							break;
//
//						}
//
//					}
//					if(jda != null) {
//						jda.getPresence().setPresence(s, act);
//						continue;
//					}
//
//					logger.info("Bot Is Offline");
//					continue;
//
//				} catch(Exception e) {
//					logger.error("Input error, please try again");
//					break;
//				}
//
//			}
//
//			if(cmd.equals("info")) {
//				if(jda != null) {
//					String tag = jda.getSelfUser().getAsTag();
//					String avatarUrl = jda.getSelfUser().getAvatarUrl();
//					OffsetDateTime timeCreated = jda.getSelfUser().getTimeCreated();
//					String id = jda.getSelfUser().getId();
//					System.out.println("Tag of the bot: " + tag);
//					System.out.println("Avatar url: " + avatarUrl);
//					System.out.println("Time created: " + timeCreated);
//					System.out.println("Id: " + id);
//					System.out.println("Shard info: " + jda.getShardInfo().getShardString());
//					System.out.println("Guilds: " + jda.getGuilds().size());
//					continue;
//				}
//				logger.info("Bot Is Offline");
//				continue;
//			}
//
//			if(cmd.equals("help")) {
//				System.out.println( 
//						"Option 1: start <msg> = To start the bot\n" +
//								"Option 2: shutdown = To shutdown the bot\n" +
//								"Option 3: presence = To set presence\n" +
//								"Option 4: info = To get info of the bot\n" +
//						"Option 5: help = UBCL help (this)");
//				continue;
//			}
//
//			if(!cmd.equals(""))
//				logger.debug("Unknown Command");
//
//		}
	}

	static void sendMessage(TextChannel chnl, String msg, String emj, String []emjs) {
		chnl.sendMessage(msg).queue(message -> {
			for(int i = 0; i < emjs.length; i++) {
				message.addReaction(emjs[i]).queue();
			}
		});
	}

	static void sendMessageEmbed(TextChannel chnl, EmbedBuilder emd, String emj, String []emjs) {
		chnl.sendMessageEmbeds(emd.build()).queue(message -> {
			for(int i = 0; i < emjs.length; i++) {
				message.addReaction(emjs[i]).queue();
			}
		});
	}
}
