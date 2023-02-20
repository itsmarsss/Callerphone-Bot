package com.marsss.callerphone.users.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class DeductCredits implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        if (!e.getAuthor().getId().equals(Callerphone.config.getOwnerID())) {
            e.getMessage().reply(ToolSet.CP_EMJ + "Run this command once you own this bot...").queue();
            return;
        }
        try {
            final String[] ARGS = e.getMessage().getContentRaw().split("\\s+");
            final List<User> MENTIONS = e.getMessage().getMentionedUsers();
            final User USER = MENTIONS.size() > 0 ? MENTIONS.get(0) : e.getAuthor();
            int amount;
            amount = Integer.parseInt(ARGS[1]);
            e.getMessage().reply(deductCredits(USER, amount)).queue();
        } catch (Exception ex) {
            ex.printStackTrace();
            e.getMessage().reply(ToolSet.CP_EMJ + "`" + Callerphone.config.getPrefix() + "deductcreds <amount> <@user>`").queue();
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
    }

    private String deductCredits(User user, int amount) {
        Callerphone.storage.reward(user, (-amount));
        return ToolSet.CP_EMJ + "Deducted `\u00A9 " + amount + "` from " + user.getAsMention();
    }

    @Override
    public String getHelp() {
        return "Admin command";
    }

    @Override
    public String[] getTriggers() {
        return "deduct,takecreds,deductcreds".split(",");
    }
}
