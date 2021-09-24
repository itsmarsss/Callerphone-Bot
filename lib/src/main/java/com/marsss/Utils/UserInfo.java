package com.marsss.Utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

public class UserInfo {
	public static MessageEmbed userinfo(Member mmbr) {

		String NAME = mmbr.getEffectiveName();
		String TAG = mmbr.getUser().getName() + "#" + mmbr.getUser().getDiscriminator();
		String GUILD_JOIN_DATE = mmbr.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String DISCORD_JOINED_DATE = mmbr.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String ID = mmbr.getUser().getId();
		String ROLES = "";
		String AVATAR = mmbr.getUser().getAvatarUrl();

		for (Role r : mmbr.getRoles() ) {
			ROLES += r.getAsMention() + ", ";
		}
		if (ROLES.length() > 0)
			ROLES = ROLES.substring(0, ROLES.length()-2);
		else
			ROLES = "No roles on this server.";

		if (AVATAR == null) {
			AVATAR = "No Avatar";
		}

		EmbedBuilder UsrInfEmd = new EmbedBuilder()
				.setColor(Color.GREEN)
				.setDescription("🗂️ **User information for " + mmbr.getUser().getName() + ":**")
				.addField("Name", NAME, false)
				.addField("Tag", TAG, false)
				.addField("ID", ID, false)
				.addField("Roles", ROLES, false)
				.addField("Joined Guild", GUILD_JOIN_DATE, false)
				.addField("Joined Discord", DISCORD_JOINED_DATE, false)
				.addField("Avatar URL", "[link](" + AVATAR + ")", false);

		if (AVATAR != "No Avatar") {
			UsrInfEmd.setThumbnail(AVATAR);
		}

		return UsrInfEmd.build();
	}

}
