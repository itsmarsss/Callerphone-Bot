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

import java.awt.Color;
import java.util.List;

public class UserInfo implements ISlashCommand {

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping option = e.getOption("member");
        Member member = option != null ? option.getAsMember() : e.getMember();
        if (member == null) {
            e.replyEmbeds(EmbedHelpers.error("Could not resolve that member.")).setEphemeral(true).queue();
            return;
        }
        e.replyEmbeds(userInfo(member)).queue();
    }

    public MessageEmbed userInfo(Member member) {
        List<Role> roles = member.getRoles();
        Color color = roles.isEmpty() ? null : roles.get(0).getColor();
        String avatar = member.getUser().getEffectiveAvatarUrl();

        return EmbedHelpers.base()
                .setColor(color)
                .setDescription(":dividers: **User information for " + member.getAsMention() + ":**")
                .addField("Name", member.getEffectiveName(), true)
                .addField("Tag", member.getUser().getName(), true)
                .addField("Permissions", EmbedHelpers.joinPermissions(member.getPermissions()), false)
                .addField("Roles", EmbedHelpers.joinMentions(roles, "No roles on this server."), false)
                .addField("Joined Guild", member.getTimeJoined().format(EmbedHelpers.DATE_FMT), true)
                .addField("Joined Discord", member.getUser().getTimeCreated().format(EmbedHelpers.DATE_FMT), true)
                .addField("Avatar URL", "[link](" + avatar + ")", true)
                .addField("Owner", String.valueOf(member.isOwner()), true)
                .addField("Verifying", String.valueOf(member.isPending()), true)
                .setThumbnail(avatar)
                .setFooter("ID: " + member.getId())
                .build();
    }

    @Override
    public String getHelp() {
        return "</userinfo:1075169261301006376> <@user/empty> - Get information about this member!";
    }

    @Override
    public String getName() {
        return "userinfo";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Get information about a member")
                .addOptions(new OptionData(OptionType.USER, "member", "Target member"))
                .setContexts(InteractionContextType.GUILD);
    }
}
