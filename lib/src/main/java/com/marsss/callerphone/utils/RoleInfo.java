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

@SuppressWarnings("ConstantConditions")
public class RoleInfo implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        final String[] ARGS = CONTENT.split("\\s+");
        final List<Role> ROLES = MESSAGE.getMentionedRoles();
        Role role;

        try {
            role = Callerphone.jda.getRoleById(Long.parseLong(ARGS[1]));
        }catch(Exception ex) {
            ex.printStackTrace();
            role = null;
        }

        if(ROLES.size() > 0) {
            if(role == null)
                role = ROLES.get(0);
        }else if(role == null) {
            MESSAGE.reply("Please specify a role").queue();
            return;
        }

        e.getMessage().replyEmbeds(roleInfo(role)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        e.replyEmbeds(roleInfo(e.getOption("role").getAsRole())).queue();
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

        EmbedBuilder RleInfEmd = new EmbedBuilder()
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

        return RleInfEmd.build();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "roleinfo <@role/id>` - Get information about this role.";
    }

    @Override
    public String[] getTriggers() {
        return "roleinfo,rolinfo,roleinf,rolinf".split(",");
    }
}
