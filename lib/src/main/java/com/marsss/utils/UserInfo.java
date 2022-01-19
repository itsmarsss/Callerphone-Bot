package com.marsss.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.Bot;

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
			if(ROLES.length() > 1024) {
				ROLES = ROLES.substring(0, 1000);
				ROLES = ROLES.substring(0, ROLES.lastIndexOf(","));
				ROLES = ROLES + "` + " + (mmbr.getRoles().size() - (ROLES.length() - ROLES.replaceAll("@", "").length())) + " more`";
			}
		}else
			ROLES = "No roles on this server.";

		if (AVATAR == null) {
			AVATAR = "No Avatar";
		}


		EmbedBuilder UsrInfEmd = new EmbedBuilder()
				.setColor(COLOR)
				.setDescription("🗂 **User information for " + mmbr.getAsMention() + ":**")
				.addField("Name", NAME, true)
				.addField("Tag", TAG, true)
				.addField("Permissions", PERMISSIONS, false)
				.addField("Roles", ROLES, false)
				.addField("Joined Guild", GUILD_JOIN_DATE, true)
				.addField("Joined Discord", DISCORD_JOINED_DATE, true)
				.addField("Avatar URL", "[link](" + AVATAR + ")", true)
				.addField("Owner", ISOWNER, true)
				.addField("Verifying", ISPENDING, true)
				.setFooter("ID: " + ID);

		if (AVATAR != "No Avatar") {
			UsrInfEmd.setThumbnail(AVATAR);
		}

		return UsrInfEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "userinfo <@user/id/empty>` - Get information about this member!";
	}

}
