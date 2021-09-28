package com.marsss.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class ChannelInfo {

	public static MessageEmbed textchannelinfo(TextChannel chnl) {
		Color COLOR = Colour.randColor();
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
				.setColor(COLOR)
				.setDescription("🗂�? **Channel information for " + chnl.getAsMention() + ":**")
				.addField("Name", NAME, false)
				.addField("Topic", TOPIC, false)
				.addField("Type", TYPE, false)
				.addField("Slowmode", SLOWMODE + "s", false)
				.addField("ID", ID, false)
				.addField("Creation Date", DATE_CREATED, false)
				.addField("Parent", PARENT, false)
				.addField("Position", POSITION, false)
				.addField("Is News", ISNEWS, true)
				.addField("Is NSFW", ISNSFW, true)
				.addField("Is Synced", ISSYNCED, true);

		return ChnlInfEmd.build();
	}

	public static MessageEmbed voicechannelinfo(VoiceChannel chnl) {
		Color COLOR = Colour.randColor();
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
				.setColor(COLOR)
				.setDescription("🗂�? **Channel information for " + chnl.getAsMention() + ":**")
				.addField("Name", NAME, false)
				.addField("Type", TYPE, false)
				.addField("Bitrate", BITRATE + "kbps", false)
				.addField("Region", REGION, false)
				.addField("User Limit", USERLIMIT, false)
				.addField("ID", ID, false)
				.addField("Creation Date", DATE_CREATED, false)
				.addField("Parent", PARENT, false)
				.addField("Position", POSITION, false)
				.addField("Is Synced", ISSYNCED, false);

		return ChnlInfEmd.build();
	}
	
	public static String getHelp() {
		return "`channelinfo` <channel>` - Get information about this channel!";
	}
}
