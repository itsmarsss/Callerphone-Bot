package com.marsss.callerphone.utils;

import com.marsss.commandType.ISlashCommand;
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

public class RoleInfo implements ISlashCommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        Role role = e.getMember().getRoles().get(0);

        if(!e.getOptions().isEmpty()) {
            role = e.getOptions().get(0).getAsRole();
        }

        e.replyEmbeds(roleInfo(role)).queue();
    }

    private MessageEmbed roleInfo(Role role) {
        final Color COLOR = role.getColor();
        final String NAME = role.getName();
        final String ID = role.getId();
        final String DATE_CREATED = role.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        StringBuilder PERMISSIONS = new StringBuilder();
        StringBuilder MEMBERS_WITH_ROLE = new StringBuilder();
        final String POSITION = String.valueOf(role.getGuild().getRoles().size() - role.getPosition());
        final String ISHOISTED = String.valueOf(role.isHoisted());
        final String ISMANAGED = String.valueOf(role.isManaged());
        final String ISMENTIONABLE = String.valueOf(role.isMentionable());
        final String ISPUBLICROLE = String.valueOf(role.isPublicRole());

        for (Permission p : role.getPermissions()) {
            PERMISSIONS.append(p.getName()).append(", ");
        }
        if (PERMISSIONS.length() > 0) {
            PERMISSIONS = new StringBuilder(PERMISSIONS.substring(0, PERMISSIONS.length() - 2));
        } else
            PERMISSIONS = new StringBuilder("No permissions.");


        for (Member m : role.getGuild().getMembersWithRoles(role)) {
            MEMBERS_WITH_ROLE.append(m.getAsMention()).append(", ");
        }
        if (MEMBERS_WITH_ROLE.length() > 0) {
            MEMBERS_WITH_ROLE = new StringBuilder(MEMBERS_WITH_ROLE.substring(0, MEMBERS_WITH_ROLE.length() - 2));
            if (MEMBERS_WITH_ROLE.length() > 1024) {
                MEMBERS_WITH_ROLE = new StringBuilder(MEMBERS_WITH_ROLE.substring(0, 1000));
                MEMBERS_WITH_ROLE = new StringBuilder(MEMBERS_WITH_ROLE.substring(0, MEMBERS_WITH_ROLE.lastIndexOf(",")));
                MEMBERS_WITH_ROLE.append("` + ").append(role.getGuild().getMembers().size() - (MEMBERS_WITH_ROLE.length() - MEMBERS_WITH_ROLE.toString().replaceAll("@", "").length())).append(" more`");
            }
        } else
            MEMBERS_WITH_ROLE = new StringBuilder("No member has this Role.");

        EmbedBuilder roleInfoEmbed = new EmbedBuilder()
                .setColor(COLOR)
                .setDescription(":pencil: **Role information for " + role.getAsMention() + ":**")
                .addField("Name", NAME, false)
                .addField("Permissions", PERMISSIONS.toString(), false)
                .addField("Members With Role", MEMBERS_WITH_ROLE.toString(), false)
                .addField("Creation Date", DATE_CREATED, false)
                .addField("Position", POSITION, false)
                .addField("Apart from online", ISHOISTED, true)
                .addField("Integration", ISMANAGED, true)
                .addField(" ", " ", true)
                .addField("Mentionable", ISMENTIONABLE, true)
                .addField("Public Role", ISPUBLICROLE, true)
                .addField(" ", " ", true)
                .setFooter("ID: " + ID);

        return roleInfoEmbed.build();
    }

    @Override
    public String getHelp() {
        return "</roleinfo:1075169248630034512> <@role> - Get information about this role.";
    }

    @Override
    public String[] getTriggers() {
        return "roleinfo,rolinfo,roleinf,rolinf".split(",");
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getTriggers()[0], getHelp().split(" - ")[1])
                .addOptions(
                        new OptionData(OptionType.ROLE, "role", "Target role")
                )
                .setGuildOnly(true);
    }
}
