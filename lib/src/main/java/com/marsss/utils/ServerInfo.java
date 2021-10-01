package com.marsss.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ServerInfo {

	public static MessageEmbed serverinfo(Guild gld) {
		Color COLOR = Colour.randColor();

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
				.setColor(COLOR)
				.setDescription("🗂 **Server information for " + NAME + ":**")
				.addField("General Information", 
						"__Name:__ " + NAME + 
						"\n__Description:__ " + DESCRIPTION +
						"\n__Icon URL:__ [Icon](" + ICONURL + ")" +
						"\n__Banner URL:__ [Banner](" + BANNERURL + ")",
						false)

				.addField("Members", 
						"__Member Count:__ " + MEMBERS +
						"\n__Owner:__ " + OWNER,
						false)

				.addField("Boosts", 
						"__Boost Count:__ " + BOOSTCOUNT +
						"\n__Boost Tier:__ " + BOOSTTIER, 
						false)

				.addField("Categories", 
						"__Category Count:__ " + CATEGORIES +
						"\n__Channel Count:__ " + CHANNELS +
						"\n__TextChannel Count:__ " + TEXTCHANNELS +
						"\n__VoiceChannel Count:__ " + VOICECHANNELS +
						"\n__StageChannel Count:__ " + STAGECHANNELS +
						"\n__System Channel:__ " + SYSTEMCHANNEL +
						"\n__Rules Channel:__ " + RULESCHANNEL +
						"\n__Community Update Channel__ " + COMMUNITYUPDATESCHANNEL,
						false)

				.addField("Roles", "__Role Count:__ " + ROLES, false)

				.addField("AFK",
						"__AFK Channel:__ " + AFKCHANNEL +
						"\n__AFK Timeout:__ " + AFKTIMEOUT,
						false)

				.addField("Region", 
						"__Region:__ " + REGION + 
						"\n__Creation Date:__ " + DATECREATED,
						false);

		SvrInfEmd.setThumbnail(ICONURL);
		return SvrInfEmd.build();
	}

	public static String getHelp() {
		return "`serverinfo` - Get information about the server.";
	}

}
