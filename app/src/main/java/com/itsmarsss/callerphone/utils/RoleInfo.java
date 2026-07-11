package com.itsmarsss.callerphone.utils;

import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public class RoleInfo implements ISlashCommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping option = e.getOption("role");
        Role role;
        if (option != null) {
            role = option.getAsRole();
        } else {
            Member member = e.getMember();
            if (member == null || member.getRoles().isEmpty()) {
                e.replyEmbeds(EmbedHelpers.error("Provide a role or have at least one role.")).setEphemeral(true).queue();
                return;
            }
            role = member.getRoles().get(0);
        }

        e.replyEmbeds(roleInfo(role)).queue();
    }

    private MessageEmbed roleInfo(Role role) {
        List<Member> members = role.getGuild().getMembersWithRoles(role);
        int positionFromTop = role.getGuild().getRoles().size() - role.getPosition();

        return EmbedHelpers.base()
                .setColor(EmbedHelpers.roleColor(role))
                .setDescription(":pencil: **Role information for " + role.getAsMention() + ":**")
                .addField("Name", role.getName(), false)
                .addField("Permissions", EmbedHelpers.joinPermissions(role.getPermissions()), false)
                .addField("Members With Role",
                        members.isEmpty()
                                ? "No member has this Role."
                                : members.size() + " members\n" + EmbedHelpers.joinMentions(members, "None"),
                        false)
                .addField("Creation Date", role.getTimeCreated().format(EmbedHelpers.DATE_FMT), false)
                .addField("Position", String.valueOf(positionFromTop), false)
                .addField("Hoisted", String.valueOf(role.isHoisted()), true)
                .addField("Integration", String.valueOf(role.isManaged()), true)
                .addField("Mentionable", String.valueOf(role.isMentionable()), true)
                .addField("Public Role", String.valueOf(role.isPublicRole()), true)
                .setFooter("ID: " + role.getId())
                .build();
    }

    @Override
    public String getHelp() {
        return "</roleinfo:1075169248630034512> <@role> - Get information about this role.";
    }

    @Override
    public String getName() {
        return "roleinfo";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get information about a role")
                .addOptions(new OptionData(OptionType.ROLE, "role", "Target role"))
                .setContexts(InteractionContextType.GUILD);
    }
}
