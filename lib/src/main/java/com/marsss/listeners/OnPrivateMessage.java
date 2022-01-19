package com.marsss.listeners;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import com.marsss.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.AttachmentOption;

public class OnPrivateMessage extends ListenerAdapter {
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {

		if(event.getAuthor().isBot() || event.getAuthor().isSystem())
			return;

		final User MEMBER = event.getAuthor();
		final Message MESSAGE = event.getMessage();

		String CONTENT = MESSAGE.getContentRaw();

		final String args[] = CONTENT.split("\\s+");

		boolean isAdmin = Bot.admin.contains(event.getAuthor().getId());

		if(CONTENT.startsWith(Bot.Prefix + "help mod")) {
			String TITLE = "Mod";
			String DESC = "You do not have permission to access this category.";

			if(isAdmin) {
				DESC = CommandListener.adminHelp() + "\n"
						+ CommandListener.blacklistHelp() + "\n"
						+ CommandListener.supportHelp() + "\n"
						+ CommandListener.showItemsHelp();
			}

			EmbedBuilder HelpEmd = new EmbedBuilder()
					.setTitle(TITLE)
					.setDescription(DESC)
					.setFooter("Hope you found this useful!", Bot.jda.getSelfUser().getAvatarUrl())
					.setColor(new Color(114, 137, 218));

			sendPrivateEmbed(MEMBER, HelpEmd.build());
			return;
		}

		if(CONTENT.toLowerCase().startsWith(Bot.Prefix)) {
			try {

				switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {
				
				case "blackedlist":
					sendPrivateFile(MEMBER, new File(Bot.parent + "/blacklist.txt"), "Callerphone Blacklist:");
					return;

				case "prefixlist":
					sendPrivateFile(MEMBER, new File(Bot.parent + "/prefix.txt"), "Callerphone Prefix list:");
					return;

				case "infolist":
					sendPrivateFile(MEMBER, new File(Bot.parent + "/info.txt"), "Callerphone Info list:");
					return;

				case "modlist":
					sendPrivateFile(MEMBER, new File(Bot.parent + "/admin.txt"), "Callerphone Moderator list:");
					return;

				case "filterlist":
					sendPrivateFile(MEMBER, new File(Bot.parent + "/filter.txt"), "Callerphone Filter list:");
					return;
					
				}

				String id = args[1];
				switch (args[0].toLowerCase().replace(Bot.Prefix, "")) {

				case "blacklist":
					if(Bot.blacklist.contains(id)) {
						MESSAGE.reply("ID blacklisted already").queue();
					}else {
						Bot.blacklist.add(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String m : Bot.blacklist) {
								sb.append(m + "\n");
							}
							FileWriter myWriter = new FileWriter(Bot.parent + "/blacklist.txt");
							myWriter.write(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` added to blacklist").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				case "prefix":
					if(Bot.prefix.containsKey(id)) {
						MESSAGE.reply("ID has prefix already (" + Bot.prefix.get(id) + ")").queue();
					}else {
						String prefix = args[2];
						if(prefix.length() > 15) {
							MESSAGE.reply("Prefix too long (max. length is 15 chars)").queue();
							break;
						}
						Bot.prefix.put(id, prefix);
						StringBuilder sb = new StringBuilder();
						try {
							for(String key : Bot.prefix.keySet()) {
								sb.append(key + "|" + Bot.prefix.get(key) + "\n");
							}
							FileWriter myWriter = new FileWriter(Bot.parent + "/prefix.txt");
							myWriter.write(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` now has prefix `" + prefix + "`").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				case "mod":
					if(Bot.admin.contains(id)) {
						MESSAGE.reply("ID is mod already").queue();
					}else {
						Bot.admin.add(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String m : Bot.admin) {
								sb.append(m + "\n");
							}
							FileWriter myWriter = new FileWriter(Bot.parent + "/admin.txt");
							myWriter.write(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` added to mod list").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;




				case "rblacklist":
					if(!Bot.blacklist.contains(id)) {
						MESSAGE.reply("ID not blacklisted").queue();
					}else {
						Bot.blacklist.remove(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String m : Bot.blacklist) {
								sb.append(m + "\n");
							}
							PrintWriter myWriter = new PrintWriter(Bot.parent + "/blacklist.txt");
							myWriter.print(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` removed from blacklist").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				case "rprefix":
					if(!Bot.prefix.containsKey(id)) {
						MESSAGE.reply("ID does not have a prefix").queue();
					}else {
						Bot.prefix.remove(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String key : Bot.prefix.keySet()) {
								sb.append(key + "|" + Bot.prefix.get(key) + "\n");
							}
							PrintWriter myWriter = new PrintWriter(Bot.parent + "/prefix.txt");
							myWriter.print(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` no longer has a prefix").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				case "rmod":
					if(!Bot.admin.contains(id)) {
						MESSAGE.reply("ID is not a mod").queue();
					}else {
						if(id.equals(Bot.owner)) {
							MESSAGE.reply("You cannot remove this mod").queue();
							break;
						}
						Bot.admin.remove(id);
						StringBuilder sb = new StringBuilder();
						try {
							for(String m : Bot.admin) {
								sb.append(m + "\n");
							}
							PrintWriter myWriter = new PrintWriter(Bot.parent + "/admin.txt");
							myWriter.print(sb.toString());
							myWriter.close();
							MESSAGE.reply("ID: `" + id + "` removed from mod list").queue();
						} catch (IOException e) {
							MESSAGE.reply("An error occured").queue();
						}
					}
					break;

				}
			} catch(Exception e) {
				MESSAGE.reply("Syntax Error").queue();
			}

			return;
		}

		normalMessage(event.getChannel(), CONTENT);
	}

	public void normalMessage(PrivateChannel channel, String msg) {
		msg = msg.replaceAll("\\s+", "%20");

		try {
			final URL url = new URL(Bot.brainURL.replace("[uid]", channel.getId()).replace("[msg]", msg));
			final Scanner sc = new Scanner(url.openStream());

			String s = sc.nextLine();

			s = s.replace("<a href=\\\"", "");
			s = s.replace("\">", " ");
			s = s.replace("<\\/a>", "");

			channel.sendMessage(replaceLast(s.replace("{\"cnt\":\"", ""), "\"}", "")).queue();
		} catch (Exception e) {
			channel.sendMessage("An error occured, if this persists please notify ").queue();
		}

	}

	public void sendPrivateFile(User user, File file, String title) {
		user.openPrivateChannel().queue((channel) ->
		{
			channel.sendFile(file, title + ".txt", AttachmentOption.SPOILER).queue();
		});
	}

	public void sendPrivateEmbed(User user, MessageEmbed embed) {
		user.openPrivateChannel().queue((channel) ->
		{
			channel.sendMessageEmbeds(embed).queue();
		});
	}

	private static String replaceLast(final String text, final String regex, final String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

}
