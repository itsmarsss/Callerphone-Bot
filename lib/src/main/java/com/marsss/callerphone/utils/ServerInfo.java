package com.marsss.callerphone.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.callerphone.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ServerInfo {

	public static MessageEmbed serverinfo(Guild gld) {
		String NAME = gld.getName();
		String DESCRIPTION = gld.getDescription();

		String ICONURL = gld.getIconUrl();
		String BANNERURL = gld.getBannerUrl();

		String MEMBERS = String.valueOf(gld.getMembers().size());
		String OWNER = gld.getOwner().getAsMention();


		String BOOSTCOUNT = String.valueOf(gld.getBoostCount());
		String BOOSTTIER = gld.getBoostTier().toString();

		String CATEGORIES = String.valueOf(gld.getCategories().size());
		String CHANNELS = String.valueOf(gld.getChannels().size());
		String TEXTCHANNELS = String.valueOf(gld.getTextChannels().size());
		String VOICECHANNELS = String.valueOf(gld.getVoiceChannels().size());
		String STAGECHANNELS = "N/A";
		String SYSTEMCHANNEL = "N/A";
		String RULESCHANNEL = "N/A";
		String COMMUNITYUPDATESCHANNEL = "N/A";

		String ROLES = "N/A";

		String AFKCHANNEL = "N/A";
		String AFKTIMEOUT = "N/A";

		if(DESCRIPTION == null)
			DESCRIPTION = "N/A";

		try {
			CHANNELS = String.valueOf(gld.getChannels().size());
			TEXTCHANNELS = String.valueOf(gld.getTextChannels().size());
			VOICECHANNELS = String.valueOf(gld.getVoiceChannels().size());
			STAGECHANNELS = String.valueOf(gld.getStageChannels().size());
			SYSTEMCHANNEL = gld.getSystemChannel().getAsMention();
			RULESCHANNEL = gld.getRulesChannel().getAsMention();
			COMMUNITYUPDATESCHANNEL = gld.getCommunityUpdatesChannel().getAsMention();

			ROLES = String.valueOf(gld.getRoles().size());

			AFKCHANNEL = gld.getAfkChannel().getAsMention();
			AFKTIMEOUT = String.valueOf(gld.getAfkTimeout().getSeconds()) + "s";
		}catch(Exception e) {}

		@SuppressWarnings("deprecation")
		String REGION = gld.getRegionRaw();
		String DATECREATED = gld.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);

		EmbedBuilder SvrInfEmd = new EmbedBuilder()
				.setColor(Color.cyan)
				.setDescription("💽 **Server information for " + NAME + ":**")
				.addField("General Information", 
						"Name: " + NAME + 
						"\nDescription: " + DESCRIPTION +
						"\nIcon URL: [Icon](" + ICONURL + ")" +
						"\nBanner URL: [Banner](" + BANNERURL + ")",
						false)

				.addField("Categories", 
						"Category Count: " + CATEGORIES +
						"\nChannel Count: " + CHANNELS +
						"\nTextChannel Count: " + TEXTCHANNELS +
						"\nVoiceChannel Count: " + VOICECHANNELS +
						"\nStageChannel Count: " + STAGECHANNELS +
						"\nSystem Channel: " + SYSTEMCHANNEL +
						"\nRules Channel: " + RULESCHANNEL +
						"\nCommunity Update Channel: " + COMMUNITYUPDATESCHANNEL,
						false)

				.addField("Members", 
						"Member Count: " + MEMBERS +
						"\nOwner: " + OWNER,
						true)

				.addField("Boosts", 
						"Boost Count: " + BOOSTCOUNT +
						"\nBoost Tier: " + BOOSTTIER, 
						true)

				.addField("Roles", "Role Count: " + ROLES, 
						true)

				.addField("Region", 
						"Region: " + REGION + 
						"\nCreation Date: " + DATECREATED,
						true)

				.addField("AFK",
						"AFK Channel: " + AFKCHANNEL +
						"\nAFK Timeout: " + AFKTIMEOUT,
						true)
				
				.setFooter("ID: " + gld.getId());

		SvrInfEmd.setThumbnail(ICONURL);
		return SvrInfEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "serverinfo` - Get information about the server.";
	}

}
