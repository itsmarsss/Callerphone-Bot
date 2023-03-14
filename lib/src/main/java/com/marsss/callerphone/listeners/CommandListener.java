package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.bot.Advertisement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Random;

public class CommandListener extends ListenerAdapter {
    public void onGuildMessageReceived(MessageReceivedEvent event) {
        final Message MESSAGE = event.getMessage();
        if (MESSAGE.isWebhookMessage())
            return;

        if (!event.getChannel().canTalk())
            return;

        event.getMessage().reply("We have completely migrated to slash commands, please run /help for more information.").queue();

        final Member MEMBER = event.getMember();

        final String CONTENT = MESSAGE.getContentRaw();

        final String[] ARGS = CONTENT.split("\\s+");

        if (MEMBER.getUser().isBot() || MEMBER.getUser().isSystem())
            return;

        if (CONTENT.contains(Callerphone.jda.getSelfUser().getId())) {
            MESSAGE.reply("My prefix is `" + Callerphone.config.getPrefix() + "`, do `" + Callerphone.config.getPrefix() + "help` for a list of commands!").queue();
            return;
        }

        if (!ARGS[0].toLowerCase().startsWith(Callerphone.config.getPrefix()))
            return;

        String trigger = ARGS[0].toLowerCase().replace(Callerphone.config.getPrefix(), "");

        try {
            if (Callerphone.cmdMap.containsKey(trigger)) {

                if (!Storage.hasUser(event.getAuthor().getId())) {
                    ToolSet.sendPPAndTOS(event);
                    return;
                }

                if (Storage.isBlacklisted(event.getAuthor().getId())) {
                    MESSAGE.reply("Sorry you are blacklisted, submit an appeal in our support server " + Callerphone.config.getSupportServer()).queue();
                    return;
                }

                if (System.currentTimeMillis() - Storage.getCmdCooldown(event.getAuthor()) < ToolSet.COMMAND_COOLDOWN) {
                    ToolSet.sendCommandCooldown(event);
                    return;
                }

                Storage.updateCmdCooldown(event.getAuthor());

                Storage.reward(event.getAuthor(), 1);
                Storage.addExecute(event.getAuthor(), 1);

                Callerphone.cmdMap.get(trigger).runCommand(event);

                if (new Random().nextInt(9) == 0) {
                    event.getMessage().replyEmbeds(Advertisement.generateAd()).queue();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sendError(event.getMessage(), ex);
        }

    }

    public static void sendError(Message message, Exception error) {
        message.reply(String.format(Response.ERROR_MSG.toString(), error.toString())).queue();
    }

    public static String adminHelp() {
        return "`" + Callerphone.config.getPrefix() + "mod <id>` - Adds id to mod list.\n" +
                "`" + Callerphone.config.getPrefix() + "rmod <id>` - Removes id from mod list.";
    }

    public static String blacklistHelp() {
        return "`" + Callerphone.config.getPrefix() + "blacklist <id>` - Adds id to blacklist.\n" +
                "`" + Callerphone.config.getPrefix() + "rblacklist <id>` - Removes id from blacklist.";
    }

    public static String supportHelp() {
        return "`" + Callerphone.config.getPrefix() + "prefix <id> <prefix>` - Give user a prefix.\n" +
                "`" + Callerphone.config.getPrefix() + "rprefix <id>` - Removes user prefix.";
    }

    public static String showItemsHelp() {
        return "`" + Callerphone.config.getPrefix() + "blackedlist` - Shows all black listed users.\n" +
                "`" + Callerphone.config.getPrefix() + "prefixlist` - Shows all prefixes for users.\n" +
                "`" + Callerphone.config.getPrefix() + "infolist` - Shows all info for startup.\n" +
                "`" + Callerphone.config.getPrefix() + "modlist` - Shows all moderators.\n" +
                "`" + Callerphone.config.getPrefix() + "filterlist` - Shows all chat filters.";
    }

}
