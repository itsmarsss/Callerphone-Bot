package com.marsss.callerphone.dormant;

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
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;

@SuppressWarnings("ConstantConditions")
public class UserInfo implements ICommand {

	@Override
	public void runCommand(MessageReceivedEvent e) {
		final Message MESSAGE = e.getMessage();
		final String CONTENT = MESSAGE.getContentRaw();
		final String[] ARGS = CONTENT.split("\\s+");

		final List<Member> USERS = MESSAGE.getMentionedMembers();
		Member USER;

		try {
			USER = e.getGuild().getMemberById(Long.parseLong(ARGS[1]));
		} catch (Exception ex) {
			ex.printStackTrace();
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
	public void runSlash(SlashCommandEvent e) {
		e.replyEmbeds(userinfo(e.getOption("member").getAsMember())).queue();
	}

	public MessageEmbed userinfo(Member mmbr) {
		Color COLOR = null;
		final String NAME = mmbr.getEffectiveName();
		final String TAG = mmbr.getUser().getAsTag();
		final String GUILD_JOIN_DATE = mmbr.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		final String DISCORD_JOINED_DATE = mmbr.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
		final String ID = mmbr.getUser().getId();
		StringBuilder PERMISSIONS = new StringBuilder();
		StringBuilder ROLES = new StringBuilder();
		String AVATAR = mmbr.getUser().getAvatarUrl();
		final String ISOWNER = String.valueOf(mmbr.isOwner());
		final String ISPENDING = String.valueOf(mmbr.isPending());

		for (Permission p : mmbr.getPermissions()) {
			PERMISSIONS.append(p.getName()).append(", ");
		}
		if (PERMISSIONS.length() > 0) {
			PERMISSIONS = new StringBuilder(PERMISSIONS.substring(0, PERMISSIONS.length() - 2));
		}else
			PERMISSIONS = new StringBuilder("No permissions.");


		for (Role r : mmbr.getRoles() ) {
			ROLES.append(r.getAsMention()).append(", ");
		}
		if (ROLES.length() > 0) {
			ROLES = new StringBuilder(ROLES.substring(0, ROLES.length() - 2));
			COLOR = mmbr.getRoles().get(0).getColor();
			if(ROLES.length() > 1024) {
				ROLES = new StringBuilder(ROLES.substring(0, 1000));
				ROLES = new StringBuilder(ROLES.substring(0, ROLES.lastIndexOf(",")));
				ROLES.append("` + ").append(mmbr.getRoles().size() - (ROLES.length() - ROLES.toString().replaceAll("@", "").length())).append(" more`");
			}
		}else
			ROLES = new StringBuilder("No roles on this server.");

		if (AVATAR == null) {
			AVATAR = "No Avatar";
		}


		EmbedBuilder UsrInfEmd = new EmbedBuilder()
				.setColor(COLOR)
				.setDescription(":dividers: **User information for " + mmbr.getAsMention() + ":**")
				.addField("Name", NAME, true)
				.addField("Tag", TAG, true)
				.addField("Permissions", PERMISSIONS.toString(), false)
				.addField("Roles", ROLES.toString(), false)
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
	public String getHelp() {
		return "`" + Callerphone.config.getPrefix() + "userinfo <@user/id/empty>` - Get information about this member!";
	}

	@Override
	public String[] getTriggers() {
		return "userinfo,userinf,usrinfo,usrinf".split(",");
	}

}
