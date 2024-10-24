package com.marsss.callerphone.utils;

import com.marsss.commandType.IFullCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserInfo implements IFullCommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        Member member = e.getMember();

        if (!e.getOptions().isEmpty()) {
            member = e.getOptions().get(0).getAsMember();
        }

        e.replyEmbeds(userInfo(member)).queue();
    }

    @Override
    public void runCommand(MessageReceivedEvent e) {
        List<Member> members = e.getMessage().getMentions().getMembers();

        Member member = e.getMember();

        if (!members.isEmpty()) {
            member = members.get(0);
        }

        e.getMessage().replyEmbeds(userInfo(member)).queue();
    }

    public MessageEmbed userInfo(Member member) {
        Color COLOR = null;
        final String NAME = member.getEffectiveName();
        final String TAG = member.getUser().getAsTag();
        final String GUILD_JOIN_DATE = member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String DISCORD_JOINED_DATE = member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String ID = member.getUser().getId();
        StringBuilder PERMISSIONS = new StringBuilder();
        StringBuilder ROLES = new StringBuilder();
        String AVATAR = member.getUser().getAvatarUrl();
        final String ISOWNER = String.valueOf(member.isOwner());
        final String ISPENDING = String.valueOf(member.isPending());

        for (Permission p : member.getPermissions()) {
            PERMISSIONS.append(p.getName()).append(", ");
        }
        if (PERMISSIONS.length() > 0) {
            PERMISSIONS = new StringBuilder(PERMISSIONS.substring(0, PERMISSIONS.length() - 2));
        } else
            PERMISSIONS = new StringBuilder("No permissions.");


        for (Role r : member.getRoles()) {
            ROLES.append(r.getAsMention()).append(", ");
        }
        if (ROLES.length() > 0) {
            ROLES = new StringBuilder(ROLES.substring(0, ROLES.length() - 2));
            COLOR = member.getRoles().get(0).getColor();
            if (ROLES.length() > 1024) {
                ROLES = new StringBuilder(ROLES.substring(0, 1000));
                ROLES = new StringBuilder(ROLES.substring(0, ROLES.lastIndexOf(",")));
                ROLES.append("` + ").append(member.getRoles().size() - (ROLES.length() - ROLES.toString().replaceAll("@", "").length())).append(" more`");
            }
        } else
            ROLES = new StringBuilder("No roles on this server.");

        if (AVATAR == null) {
            AVATAR = "No Avatar";
        }


        EmbedBuilder userInfoEmbed = new EmbedBuilder()
                .setColor(COLOR)
                .setDescription(":dividers: **User information for " + member.getAsMention() + ":**")
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
            userInfoEmbed.setThumbnail(AVATAR);
        }

        return userInfoEmbed.build();
    }

    @Override
    public String getHelp() {
        return "</userinfo:1075169261301006376> <@user/empty> - Get information about this member!";
    }

    @Override
    public String[] getTriggers() {
        return "userinfo,userinf,usrinfo,usrinf".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .addOptions(
                        new OptionData(OptionType.USER, "member", "Target member")
                )
                .setGuildOnly(true);
    }
}
