package com.itsmarsss.callerphone.users.commands;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.commandType.ITextCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RewardCredits implements ITextCommand {
    private static final Logger logger = LoggerFactory.getLogger(RewardCredits.class);

    @Override
    public void runCommand(MessageReceivedEvent e) {
        if (!e.getAuthor().getId().equals(Callerphone.config.getOwnerID())) {
            e.getMessage().reply(ToolSet.CP_EMJ + "Run this command once you own this bot...").queue();
            return;
        }

        try {
            String[] args = e.getMessage().getContentRaw().split("\\s+");
            if (args.length < 2) {
                e.getMessage().reply(ToolSet.CP_EMJ + "`reward <amount> [@user]`").queue();
                return;
            }

            int amount = Integer.parseInt(args[1]);
            List<User> mentions = e.getMessage().getMentions().getUsers();
            User user = !mentions.isEmpty() ? mentions.get(0) : e.getAuthor();

            Users.reward(user.getId(), amount);
            e.getMessage().reply(ToolSet.CP_EMJ + "Rewarded `\u23E3 " + amount + "` to " + user.getAsMention()).queue();
        } catch (NumberFormatException ex) {
            e.getMessage().reply(ToolSet.CP_EMJ + "Amount must be a number. `reward <amount> [@user]`").queue();
        } catch (Exception ex) {
            logger.error("reward credits failed", ex);
            e.getMessage().reply(ToolSet.CP_EMJ + "`reward <amount> [@user]`").queue();
        }
    }

    @Override
    public String getHelp() {
        return "Admin command";
    }

    @Override
    public String getName() {
        return "reward";
    }
}
