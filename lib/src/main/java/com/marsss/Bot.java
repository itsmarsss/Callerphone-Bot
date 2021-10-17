package com.marsss;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

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

	public static String brainURL = "http://api.brainshop.ai/get?bid=160403&key=FFFNOBQEMnANoVn1&uid=[uid]&msg=[msg]";
	
	public static Logger logger = LoggerFactory.getLogger(Bot.class);
	
	public static final String Callerphone = "<:CallerphoneEmote:899051549173637120> ";
	public static final String ThumbsUp = "👍";

	
	public static JDA jda;
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	private static final EnumSet<GatewayIntent> intent = EnumSet.of(
			GatewayIntent.GUILD_MEMBERS,
			GatewayIntent.GUILD_MESSAGES,
			GatewayIntent.GUILD_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_VOICE_STATES,
			GatewayIntent.GUILD_PRESENCES,
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
			jda.addEventListener(new MusicListener());
			
			
			
			for(int i = 0; i < ConvoStorage.convo.length; i++) {
				ConvoStorage.convo[i] = new Convo(new ConcurrentLinkedQueue<>(), "empty", "", false);
			}


			for(int i = 0; i < AudioStorage.audio.length; i++) {
				AudioStorage.audio[i] = new Audio(new ConcurrentLinkedQueue<>(), "empty", "", new ConcurrentLinkedQueue<>(), "", "", false);
			}

			jda.awaitReady();
			
			jda.getPresence().setActivity(Activity.watching("u?help | have fun"));

			System.out.println("Server List: ");
			for(Guild g : jda.getGuilds()) {
				logger.info("Server: " + g.getName());
			}
			
			logger.info("Bot online");

			try {
				EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Status").setColor(Color.GREEN).setFooter("Hello World!").setDescription(jda.getSelfUser().getAsMention() + " is now online;" + startupmsg);
				jda.getTextChannelById("852342009288851516").sendMessageEmbeds(embedBuilder.build()).queue();
			} catch(Exception e) {
				logger.error("Error Sending Startup Message");
			}

		}catch(Exception e) {
			logger.error(e.toString());
		}

		//jda.upsertCommand(new CommandData("ping", "Get the my ping")).queue();

		//		jda.upsertCommand(new CommandData("help", "Learn more about my commands"))
		//		.addOptions(new OptionData(OptionType.STRING, "command", "The command you want to learn more about").setRequired(true))
		//		.queue();

		//		jda.upsertCommand(new CommandData("clap", "Claps your message")
		//				.addOptions(new OptionData(OptionType.STRING, "message", "The message to clap").setRequired(true))
		//				).queue();
		//		
		//		jda.upsertCommand(new CommandData("colorrgb", "COLORS!")
		//				.addOptions(new OptionData(OptionType.INTEGER, "red", "Red value").setRequired(true))
		//				.addOptions(new OptionData(OptionType.INTEGER, "green", "Green value").setRequired(true))
		//				.addOptions(new OptionData(OptionType.INTEGER, "blue", "Blue value").setRequired(true))
		//				).queue();
		//		
		//		jda.upsertCommand(new CommandData("colorhex", "COLORS!")
		//				.addOptions(new OptionData(OptionType.STRING, "hex", "Hexcode").setRequired(true))
		//				).queue();
		//		
		//		jda.upsertCommand(new CommandData("echo", "Echos your message")
		//				.addOptions(new OptionData(OptionType.STRING, "message", "The message to echo").setRequired(true))
		//				).queue();
		//		
		//		jda.upsertCommand(new CommandData("eightball", "Help you decide things")
		//				.addOptions(new OptionData(OptionType.STRING, "question", "The question to be answered").setRequired(true))
		//				).queue();
		//		
		//		jda.upsertCommand(new CommandData("pollnew", "Creates a poll for members to vote")
		//				.addOptions(new OptionData(OptionType.STRING, "question", "What are we voting for?").setRequired(true))
		//				).queue();
		//		
		//		jda.upsertCommand(new CommandData("rps", "Rock Paper Scissors")
		//				.addOptions(new OptionData(OptionType.STRING, "move", "Rock Paper or Scissors? (r,p,s)").setRequired(true))
		//				).queue();
		//jda.updateCommands().queue(); 
	}
	public static void main(String[] args) throws LoginException, InterruptedException {
		commandPrompt();
	}

	private static void commandPrompt() throws LoginException, InterruptedException {
		Scanner sc = new Scanner(System.in);

		logger.info("\n{}Command Line Loaded...{}\nWelcome to Callerphone Bot Command Line (CBCL)!");

		while(true) {
			String cmd = sc.nextLine();

			if(cmd.startsWith("start")) {
				System.out.println("Token: ");
				String TOKEN = sc.nextLine();
				logger.info("Starting Bot...");
				if(jda != null) {
					logger.info("Bot Is Online Right Now");
				} else {
					BotInit(TOKEN, cmd.replaceFirst("start", ""));
				}
				continue;
			}

			if(cmd.equals("shutdown")) {
				logger.info("Shutting Down Bot...");
				if(jda != null) {
					jda.shutdown();
				}
				logger.info("Bot Offline");
				sc.close();
				System.exit(0);
			}

			if(cmd.equals("presence")) {
				if(jda == null) {
					logger.info("Bot Is Offline");
					continue;
				}

				Activity act = null;
				logger.info("Change Presence...");
				try {
					while(true) {

						System.out.println("Activity: ");
						String msg = sc.next().toLowerCase();

						if(msg.equals("<removestatus>")) {
							act = null;
							break;
						} else if(msg.equals("competing")) {
							System.out.println("Status Message: ");
							sc.nextLine();
							String comp = sc.nextLine();
							System.out.println("Competing: " + comp);
							act = Activity.competing(comp);
							break;

						} else if(msg.equals("listening")) {
							System.out.println("Status Message: ");
							sc.nextLine();
							String song = sc.nextLine();
							System.out.println("Listening: " + song);
							act = Activity.listening(song);
							break;

						} else if(msg.equals("playing")) {
							System.out.println("Status Message: ");
							sc.nextLine();
							String game = sc.nextLine();
							System.out.println("Playing: " + game);
							act = Activity.playing(game);
							break;

						} else if(msg.equals("streaming")) {
							System.out.println("Title Message: ");
							sc.nextLine();
							String title = sc.nextLine();
							System.out.println("Stream Link: ");
							String link = sc.nextLine();
							System.out.println("Title: " + title + "\n" + "Link: " + link);
							act = Activity.streaming(title, link);
							break;

						} else if(msg.equals("watching")) {
							System.out.println("Status Message: ");
							sc.nextLine();
							String watch = sc.nextLine();
							System.out.println("Watching: " + watch);
							act = Activity.watching(watch);
							break;

						}
					}

					OnlineStatus s = null;

					while(true) {
						System.out.println("Online Status: ");
						String msg = sc.next().toLowerCase();
						
						if(msg.equals("online")) {
							s = OnlineStatus.ONLINE;
							break;

						} else if(msg.equals("idle")) {
							s = OnlineStatus.IDLE;
							break;

						} else if(msg.equals("dnd")) {
							s = OnlineStatus.DO_NOT_DISTURB;
							break;

						} else if(msg.equals("invis")) {
							s = OnlineStatus.INVISIBLE;
							break;

						}

					}

					if(jda != null) {
						jda.getPresence().setPresence(s, act);
						continue;
					}

					logger.info("Bot Is Offline");
					continue;

				} catch(Exception e) {
					logger.error("Input error, please try again");
					break;
				}

			}

			if(cmd.equals("info")) {
				if(jda != null) {
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

			if(cmd.equals("help")) {
				System.out.println(ANSI_BLUE + 
						"Option 1: start <msg> = To start the bot\n" +
						"Option 2: shutdown <msg> = To shutdown the bot\n" +
						"Option 3: estop <msg> = To emergency shutdown the bot\n" +
						"Option 4: presence = To set presence\n" +
						"Option 6: info = To get info of the bot\n" +
						"Option 7: help = UBCL help (this)" + ANSI_RESET);
				continue;
			}

			//			if (cmd.equalsIgnoreCase("updateslashcommand")) {
			//                SlashCommandHandler.updateCommands((x) -> System.out.println(MCColor.translate("&aQueued "+x.size()+" commands!")), Throwable::printStackTrace);
			//            }

			if(!cmd.equals(""))
				logger.debug("Unknown Command");

		}
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
