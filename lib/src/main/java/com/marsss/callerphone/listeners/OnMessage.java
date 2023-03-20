package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.bot.Advertisement;
import com.marsss.commandType.ITextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.File;
import java.util.Random;

public class OnMessage extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            fromGD(event);
        } else {
            fromPM(event);
        }
    }

    private void fromGD(MessageReceivedEvent event) {

        final Message MESSAGE = event.getMessage();
        if (MESSAGE.isWebhookMessage())
            return;

        if (!event.getChannel().canTalk())
            return;

        final Member MEMBER = event.getMember();

        final String CONTENT = MESSAGE.getContentRaw();

        final String[] ARGS = CONTENT.split("\\s+");

        if (MEMBER.getUser().isBot() || MEMBER.getUser().isSystem())
            return;

        if (CONTENT.contains(Callerphone.jda.getSelfUser().getId())) {
            MESSAGE.reply("My prefix is `" + Callerphone.config.getPrefix() + "`, do `/help` for a list of commands!").queue();
            return;
        }

        if (!ARGS[0].toLowerCase().startsWith(Callerphone.config.getPrefix()))
            return;

        event.getMessage().reply("We have completely migrated to slash commands, please run /help for more information.").queue();
/*

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

                ((ITextCommand) Callerphone.cmdMap.get(trigger)).runCommand(event);

                if (new Random().nextInt(9) == 0) {
                    event.getMessage().replyEmbeds(Advertisement.generateAd()).queue();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sendError(event.getMessage(), ex);
        }
        */
    }

    private void fromPM(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem())
            return;

        final User MEMBER = event.getAuthor();
        final Message MESSAGE = event.getMessage();

        String CONTENT = MESSAGE.getContentRaw();

        final String[] args = CONTENT.split("\\s+");

        boolean isAdmin = Storage.isAdmin(event.getAuthor().getId());

        if (CONTENT.startsWith(Callerphone.config.getPrefix() + "help mod")) {
            String TITLE = "Mod";
            String DESC = "You do not have permission to access this category.";

            if (isAdmin) {
                DESC = OnMessage.adminHelp() + "\n"
                        + OnMessage.blacklistHelp() + "\n"
                        + OnMessage.supportHelp() + "\n"
                        + OnMessage.showItemsHelp();
            }

            EmbedBuilder HelpEmd = new EmbedBuilder()
                    .setTitle(TITLE)
                    .setDescription(DESC)
                    .setFooter("Hope you found this useful!", Callerphone.jda.getSelfUser().getAvatarUrl())
                    .setColor(new Color(114, 137, 218));

            ToolSet.sendPrivateEmbed(MEMBER, HelpEmd.build());
            return;
        }

        if (CONTENT.toLowerCase().startsWith(Callerphone.config.getPrefix())) {
            try {
                String id = args[1];
                switch (args[0].toLowerCase().replace(Callerphone.config.getPrefix(), "")) {

                    case "blacklist":
                        if (Storage.isBlacklisted(id)) {
                            MESSAGE.reply("ID blacklisted already").queue();
                        } else {
                            Storage.addBlacklist(id);
                            MESSAGE.reply("ID: `" + id + "` added to blacklist").queue();
                        }
                        break;

                    case "prefix":
                        if (Storage.hasPrefix(id)) {
                            MESSAGE.reply("ID has prefix already (" + Storage.getPrefix(id) + ")").queue();
                        } else {
                            String prefix = args[2];
                            if (prefix.length() > 15) {
                                MESSAGE.reply("Prefix too long (max. length is 15 chars)").queue();
                                break;
                            }
                            Storage.setPrefix(id, prefix);
                            MESSAGE.reply("ID: `" + id + "` now has prefix `" + prefix + "`").queue();
                        }
                        break;

                    case "mod":
                        if (Storage.isAdmin(id)) {
                            MESSAGE.reply("ID is mod already").queue();
                        } else {
                            Storage.addAdmin(id);
                            MESSAGE.reply("ID: `" + id + "` added to mod list").queue();
                        }
                        break;


                    case "rblacklist":
                        if (!Storage.isBlacklisted(id)) {
                            MESSAGE.reply("ID not blacklisted").queue();
                        } else {
                            Storage.addUser(id);
                            MESSAGE.reply("ID: `" + id + "` removed from blacklist").queue();
                        }
                        break;

                    case "rprefix":
                        if (!Storage.hasPrefix(id)) {
                            MESSAGE.reply("ID does not have a prefix").queue();
                        } else {
                            Storage.setPrefix(id, "");
                            MESSAGE.reply("ID: `" + id + "` no longer has a prefix").queue();
                        }
                        break;

                    case "rmod":
                        if (!Storage.isAdmin(id)) {
                            MESSAGE.reply("ID is not a mod").queue();
                        } else {
                            if (id.equals(Callerphone.config.getOwnerID())) {
                                MESSAGE.reply("You cannot remove this mod").queue();
                                break;
                            }
                            Storage.addUser(id);
                            MESSAGE.reply("ID: `" + id + "` removed from mod list").queue();
                        }
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
                MESSAGE.reply("Syntax Error").queue();
            }
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
