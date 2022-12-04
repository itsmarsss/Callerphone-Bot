package com.marsss.callerphone.utils;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class UserInfo implements ICommand {

	@Override
	public void runCommand(GuildMessageReceivedEvent e) {
		final Message MESSAGE = e.getMessage();
		final String CONTENT = MESSAGE.getContentRaw();
		final String args[] = CONTENT.split("\\s+");

		final List<Member> USERS = MESSAGE.getMentionedMembers();
		Member USER;

		try {
			USER = e.getGuild().getMemberById(Long.parseLong(args[1]));
		} catch (Exception ex) {
			USER = null;
		}

		if (USERS.size() > 0) {
			if (USER == null)
				USER = USERS.get(0);
		} else if (USER == null) {
			MESSAGE.replyEmbeds(userinfo(e.getMember())).queue();
			return;
		}
		MESSAGE.replyEmbeds(userinfo(USER)).queue();
	}

	@Override
	public void runSlash(SlashCommandEvent event) {

	}

	public MessageEmbed userinfo(Member mmbr) {


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
				.setDescription(":dividers: **User information for " + mmbr.getAsMention() + ":**")
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

		if (!AVATAR.equals("No Avatar")) {
			UsrInfEmd.setThumbnail(AVATAR);
		}

		return UsrInfEmd.build();
	}

	@Override
	public String getHelpF() {
		return "`" + Callerphone.Prefix + "userinfo <@user/id/empty>` - Get information about this member!";
	}

	@Override
	public String[] getTriggers() {
		return "userinfo,userinf,usrinfo,usrinf".split(",");
	}

}
