package com.marsss.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

public class UserInfo {
	public static MessageEmbed userinfo(Member mmbr) {

		Color COLOR = null;
		String NAME = mmbr.getEffectiveName();
		String TAG = mmbr.getUser().getName() + "#" + mmbr.getUser().getDiscriminator();
		String GUILD_JOIN_DATE = mmbr.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String DISCORD_JOINED_DATE = mmbr.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String ID = mmbr.getUser().getId();
		String PERMISSIONS = "";
		String ROLES = "";
		String AVATAR = mmbr.getUser().getAvatarUrl();
		String ISOWNER = String.valueOf(mmbr.isOwner());
		String ISPENDING = String.valueOf(mmbr.isPending());
		
		for (Permission p : mmbr.getPermissions()) {
			PERMISSIONS += p.getName() + ", ";
		}
		if (PERMISSIONS.length() > 0) {
			PERMISSIONS = PERMISSIONS.substring(0, PERMISSIONS.length()-2);
		}else
			PERMISSIONS = "No permissions.";
		
		
		for (Role r : mmbr.getRoles() ) {
			ROLES += r.getAsMention() + ", ";
		}
		if (ROLES.length() > 0) {
			ROLES = ROLES.substring(0, ROLES.length()-2);
			COLOR = mmbr.getRoles().get(0).getColor();
		}else
			ROLES = "No roles on this server.";

		if (AVATAR == null) {
			AVATAR = "No Avatar";
		}


		EmbedBuilder UsrInfEmd = new EmbedBuilder()
				.setColor(COLOR)
				.setDescription("🗂 **User information for " + mmbr.getAsMention() + ":**")
				.addField("Name", NAME, false)
				.addField("Tag", TAG, false)
				.addField("ID", ID, false)
				.addField("Permissions", PERMISSIONS, false)
				.addField("Roles", ROLES, false)
				.addField("Joined Guild", GUILD_JOIN_DATE, false)
				.addField("Joined Discord", DISCORD_JOINED_DATE, false)
				.addField("Avatar URL", "[link](" + AVATAR + ")", false)
				.addField("Is Owner", ISOWNER, true)
				.addField("Is Pending", ISPENDING, true);

		if (AVATAR != "No Avatar") {
			UsrInfEmd.setThumbnail(AVATAR);
		}

		return UsrInfEmd.build();
	}

	public static String getHelp() {
		return "`userinfo` - Get information about you!\n" +
		"`userinfo <mention>` - Get information about this member!";
	}

}
