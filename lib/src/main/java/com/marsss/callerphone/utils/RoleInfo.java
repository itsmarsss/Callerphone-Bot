package com.marsss.callerphone.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import com.marsss.callerphone.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

public class RoleInfo {

	public static MessageEmbed roleinfo(Role role) {
		Color COLOR = role.getColor();
		String NAME = role.getName();
		String ID = role.getId();
		String DATE_CREATED = role.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		String PERMISSIONS = "";
		String MEMBERS_WITH_ROLE = "";
		String POSITION = String.valueOf(role.getGuild().getRoles().size() - role.getPosition());
		String ISHOISTED = String.valueOf(role.isHoisted());
		String ISMANAGED = String.valueOf(role.isManaged());
		String ISMENTIONABLE =	String.valueOf(role.isMentionable());
		String ISPUBLICROLE = String.valueOf(role.isPublicRole());

		for (Permission p : role.getPermissions()) {
			PERMISSIONS += p.getName() + ", ";
		}
		if (PERMISSIONS.length() > 0) {
			PERMISSIONS = PERMISSIONS.substring(0, PERMISSIONS.length()-2);
		}else
			PERMISSIONS = "No permissions.";


		for (Member m : role.getGuild().getMembersWithRoles(role)) {
			MEMBERS_WITH_ROLE += m.getAsMention() + ", ";
		}
		if (MEMBERS_WITH_ROLE.length() > 0) {
			MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE.substring(0, MEMBERS_WITH_ROLE.length()-2);
			if(MEMBERS_WITH_ROLE.length() > 1024) {
				MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE.substring(0, 1000);
				MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE.substring(0, MEMBERS_WITH_ROLE.lastIndexOf(","));
				MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE + "` + " + (role.getGuild().getMembers().size() - (MEMBERS_WITH_ROLE.length() - MEMBERS_WITH_ROLE.replaceAll("@", "").length())) + " more`";
			}
		}else
			MEMBERS_WITH_ROLE = "No member has this Role.";

		EmbedBuilder RleInfEmd = new EmbedBuilder()
				.setColor(COLOR)
				.setDescription("📝 **Role information for " + role.getAsMention() + ":**")
				.addField("Name", NAME, false)
				.addField("Permissions", PERMISSIONS, false)
				.addField("Members With Role", MEMBERS_WITH_ROLE, false)
				.addField("Creation Date", DATE_CREATED, false)
				.addField("Position", POSITION, false)
				.addField("Apart from online", ISHOISTED, true)
				.addField("Integration", ISMANAGED, true)
				.addField(" ", " ", true)
				.addField("Mentionable", ISMENTIONABLE, true)
				.addField("Public Role", ISPUBLICROLE, true)
				.addField(" ", " ", true)
				.setFooter("ID: " + ID);


		return RleInfEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "roleinfo <@role/id>` - Get information about this role.";
	}
}
