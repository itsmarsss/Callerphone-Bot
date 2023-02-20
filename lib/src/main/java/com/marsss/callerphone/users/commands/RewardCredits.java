package com.marsss.callerphone.users.commands;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class RewardCredits implements ICommand {

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
            e.getMessage().reply(rewardCredits(USER, amount)).queue();
        } catch (Exception ex) {
            ex.printStackTrace();
            e.getMessage().reply(ToolSet.CP_EMJ + "`" + Callerphone.config.getPrefix() + "rewardcreds <amount> <@user>`").queue();
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
    }

    private String rewardCredits(User user, int amount) {
        Callerphone.storage.reward(user, amount);
        return ToolSet.CP_EMJ + "Rewarded `\u00A9 " + amount + "` to " + user.getAsMention();
    }

    @Override
    public String getHelp() {
        return "Admin command";
    }

    @Override
    public String[] getTriggers() {
        return "reward,givecreds,rewardcreds".split(",");
    }
}
