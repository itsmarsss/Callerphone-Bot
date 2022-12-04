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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RoleInfo implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        final String args[] = CONTENT.split("\\s+");
        final List<Role> ROLES = MESSAGE.getMentionedRoles();
        Role role;

        try {
            role = Callerphone.jda.getRoleById(Long.parseLong(args[1]));
        }catch(Exception ex) {
            role = null;
        }

        if(ROLES.size() > 0) {
            if(role == null)
                role = ROLES.get(0);
        }else if(role == null) {
            MESSAGE.reply("Please specify a role").queue();
            return;
        }

        Color COLOR = role.getColor();
        String NAME = role.getName();
        String ID = role.getId();
        String DATE_CREATED = role.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String PERMISSIONS = "";
        String MEMBERS_WITH_ROLE = "";
        String POSITION = String.valueOf(role.getGuild().getRoles().size() - role.getPosition());
        String ISHOISTED = String.valueOf(role.isHoisted());
        String ISMANAGED = String.valueOf(role.isManaged());
        String ISMENTIONABLE = String.valueOf(role.isMentionable());
        String ISPUBLICROLE = String.valueOf(role.isPublicRole());

        for (Permission p : role.getPermissions()) {
            PERMISSIONS += p.getName() + ", ";
        }
        if (PERMISSIONS.length() > 0) {
            PERMISSIONS = PERMISSIONS.substring(0, PERMISSIONS.length() - 2);
        } else
            PERMISSIONS = "No permissions.";


        for (Member m : role.getGuild().getMembersWithRoles(role)) {
            MEMBERS_WITH_ROLE += m.getAsMention() + ", ";
        }
        if (MEMBERS_WITH_ROLE.length() > 0) {
            MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE.substring(0, MEMBERS_WITH_ROLE.length() - 2);
            if (MEMBERS_WITH_ROLE.length() > 1024) {
                MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE.substring(0, 1000);
                MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE.substring(0, MEMBERS_WITH_ROLE.lastIndexOf(","));
                MEMBERS_WITH_ROLE = MEMBERS_WITH_ROLE + "` + " + (role.getGuild().getMembers().size() - (MEMBERS_WITH_ROLE.length() - MEMBERS_WITH_ROLE.replaceAll("@", "").length())) + " more`";
            }
        } else
            MEMBERS_WITH_ROLE = "No member has this Role.";

        EmbedBuilder RleInfEmd = new EmbedBuilder()
                .setColor(COLOR)
                .setDescription(":pencil: **Role information for " + role.getAsMention() + ":**")
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

        e.getMessage().replyEmbeds(RleInfEmd.build()).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "roleinfo <@role/id>` - Get information about this role.";
    }

    @Override
    public String[] getTriggers() {
        return "roleinfo,rolinfo,roleinf,rolinf".split(",");
    }
}
