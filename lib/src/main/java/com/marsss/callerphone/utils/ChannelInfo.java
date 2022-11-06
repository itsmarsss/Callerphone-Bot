package com.marsss.callerphone.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class ChannelInfo {

	public static MessageEmbed textchannelinfo(TextChannel chnl) {
		String NAME = chnl.getName();
		String TOPIC = chnl.getTopic();
		String TYPE = chnl.getType().name();
		String SLOWMODE = String.valueOf(chnl.getSlowmode());
		String ID = chnl.getId();
		String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String PARENT = chnl.getParent().getAsMention();
		String POSITION = String.valueOf(chnl.getPosition());
		String ISNEWS = String.valueOf(chnl.isNews());
		String ISNSFW = String.valueOf(chnl.isNSFW());
		String ISSYNCED = String.valueOf(chnl.isSynced());

		if(NAME == null) {
			NAME = "No Name";
		}
		
		if(TOPIC == null) {
			TOPIC = "No Topic";
		}
		
		if(PARENT == null) {
			PARENT = "No Parent Category";
		}

		EmbedBuilder ChnlInfEmd = new EmbedBuilder()
				.setColor(Color.cyan)
				.setDescription("🗨️ **Channel information for " + chnl.getAsMention() + ":**")
				.addField("Name", NAME, false)
				.addField("Topic", TOPIC, true)
				.addField("Type", TYPE, true)
				.addField("Slowmode", SLOWMODE + "s", true)
				.addField("Creation Date", DATE_CREATED, true)
				.addField("Parent", PARENT, true)
				.addField("Position", POSITION, true)
				.addField("News", ISNEWS, true)
				.addField("NSFW", ISNSFW, true)
				.addField("Synced", ISSYNCED, true)
				.setFooter("ID: " + ID);

		return ChnlInfEmd.build();
	}

	public static MessageEmbed voicechannelinfo(VoiceChannel chnl) {
		String NAME = chnl.getName();
		String TYPE = String.valueOf(chnl.getType());
		String BITRATE = String.valueOf(chnl.getBitrate());
		String REGION = String.valueOf(chnl.getRegion());
		String USERLIMIT = String.valueOf(chnl.getUserLimit());
		String ID = chnl.getId();
		String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String PARENT = chnl.getParent().getAsMention();
		String POSITION = String.valueOf(chnl.getPosition());
		String ISSYNCED = String.valueOf(chnl.isSynced());

		if(NAME == null) {
			NAME = "No Name";
		}
		
		if(USERLIMIT.equals("0")) {
			USERLIMIT = "Unlimited";
		}
		
		if(PARENT == null) {
			PARENT = "No Parent Category";
		}

		EmbedBuilder ChnlInfEmd = new EmbedBuilder()
				.setColor(Color.cyan)
				.setDescription("📻 **Channel information for " + chnl.getAsMention() + ":**")
				.addField("Name", NAME, false)
				.addField("Type", TYPE, false)
				.addField("Bitrate", BITRATE + "kbps", true)
				.addField("Region", REGION, true)
				.addField("User Limit", USERLIMIT, true)
				.addField("Creation Date", DATE_CREATED, false)
				.addField("Parent", PARENT, true)
				.addField("Position", POSITION, true)
				.addField("Synced", ISSYNCED, true)
				.setFooter("ID: " + ID);

		return ChnlInfEmd.build();
	}
	
	public static MessageEmbed categorychannelinfo(Category chnl) {
		String NAME = chnl.getName();
		String TYPE = String.valueOf(chnl.getType());
		String TEXTCHANNELS = String.valueOf(chnl.getTextChannels().size());
		String VOICECHANNELS = String.valueOf(chnl.getVoiceChannels().size());
		String ID = chnl.getId();
		String DATE_CREATED = chnl.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String POSITION = String.valueOf(chnl.getPosition());

		if(NAME == null) {
			NAME = "No Name";
		}


		EmbedBuilder ChnlInfEmd = new EmbedBuilder()
				.setColor(Color.cyan)
				.setDescription("📁 **Category information for " + chnl.getAsMention() + ":**")
				.addField("Name", NAME, false)
				.addField("Type", TYPE, true)
				.addField("TextChannels", TEXTCHANNELS, true)
				.addField("VoiceChannels", VOICECHANNELS, true)
				.addField("Creation Date", DATE_CREATED, false)
				.addField("Position", POSITION, false)
				.setFooter("ID: " + ID);

		return ChnlInfEmd.build();
	}
	
	public static String getHelp() {
		return "`" + Callerphone.Prefix + "channelinfo <#channel/id/empty>` - Get information about the channel.";
	}
}
