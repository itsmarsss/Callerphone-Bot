package com.itsmarsss.callerphone.listeners;

import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Owner/moderator DM admin commands.
 */
public class OnMessageEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }
        if (!Users.isModerator(event.getAuthor().getId())) {
            return;
        }

        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String content = message.getContentRaw().trim();
        final String prefix = Callerphone.config.getPrefix();

        if (!content.toLowerCase().startsWith(prefix.toLowerCase())) {
            return;
        }

        String body = content.substring(prefix.length()).trim();
        if (body.isEmpty()) {
            return;
        }

        String[] args = body.split("\\s+");
        String command = args[0].toLowerCase();

        try {
            if (command.equals("help") && args.length > 1 && args[1].equalsIgnoreCase("mod")) {
                sendModHelp(author);
                return;
            }

            if (args.length < 2) {
                message.reply(ToolSet.CP_EMJ + "Usage: `" + prefix + command + " <id> [args]`").queue();
                return;
            }

            String id = args[1];
            switch (command) {
                case "blacklist":
                    if (Users.isBlacklisted(id)) {
                        message.reply("ID blacklisted already").queue();
                    } else {
                        Users.addBlacklist(id);
                        message.reply("ID: `" + id + "` added to blacklist").queue();
                    }
                    break;

                case "prefix":
                    if (args.length < 3) {
                        message.reply(ToolSet.CP_EMJ + "`" + prefix + "prefix <id> <prefix>`").queue();
                        return;
                    }
                    if (Users.hasPrefix(id)) {
                        message.reply("ID has prefix already (" + Users.getPrefix(id) + ")").queue();
                    } else {
                        String userPrefix = args[2];
                        if (userPrefix.length() > 15) {
                            message.reply("Prefix too long (max. length is 15 chars)").queue();
                            break;
                        }
                        Users.setPrefix(id, userPrefix);
                        message.reply("ID: `" + id + "` now has prefix `" + userPrefix + "`").queue();
                    }
                    break;

                case "mod":
                    if (Users.isModerator(id)) {
                        message.reply("ID is mod already").queue();
                    } else {
                        Users.addModerator(id);
                        message.reply("ID: `" + id + "` added to mod list").queue();
                    }
                    break;

                case "rblacklist":
                    if (!Users.isBlacklisted(id)) {
                        message.reply("ID not blacklisted").queue();
                    } else {
                        Users.addUser(id);
                        message.reply("ID: `" + id + "` removed from blacklist").queue();
                    }
                    break;

                case "rprefix":
                    if (!Users.hasPrefix(id)) {
                        message.reply("ID does not have a prefix").queue();
                    } else {
                        Users.setPrefix(id, "");
                        message.reply("ID: `" + id + "` no longer has a prefix").queue();
                    }
                    break;

                case "rmod":
                    if (!Users.isModerator(id)) {
                        message.reply("ID is not a mod").queue();
                    } else if (id.equals(Callerphone.config.getOwnerID())) {
                        message.reply("You cannot remove this mod").queue();
                    } else {
                        Users.addUser(id);
                        message.reply("ID: `" + id + "` removed from mod list").queue();
                    }
                    break;

                default:
                    // Not an admin command we handle
                    break;
            }
        } catch (Exception e) {
            ErrorHandler.handleMessageError(event, e);
        }
    }

    private void sendModHelp(User member) {
        String desc = adminHelp() + "\n" + blacklistHelp() + "\n" + supportHelp() + "\n" + showItemsHelp();
        EmbedBuilder help = new EmbedBuilder()
                .setTitle("Mod")
                .setDescription(desc)
                .setFooter("Hope you found this useful!",
                        Callerphone.selfUser != null ? Callerphone.selfUser.getAvatarUrl() : null)
                .setColor(ToolSet.COLOR);
        ToolSet.sendPrivateEmbed(member, help.build());
    }

    public static String adminHelp() {
        String p = Callerphone.config.getPrefix();
        return "`" + p + "mod <id>` - Adds id to mod list.\n" +
                "`" + p + "rmod <id>` - Removes id from mod list.";
    }

    public static String blacklistHelp() {
        String p = Callerphone.config.getPrefix();
        return "`" + p + "blacklist <id>` - Adds id to blacklist.\n" +
                "`" + p + "rblacklist <id>` - Removes id from blacklist.";
    }

    public static String supportHelp() {
        String p = Callerphone.config.getPrefix();
        return "`" + p + "prefix <id> <prefix>` - Give user a prefix.\n" +
                "`" + p + "rprefix <id>` - Removes user prefix.";
    }

    public static String showItemsHelp() {
        String p = Callerphone.config.getPrefix();
        return "`" + p + "blackedlist` - Shows all black listed users.\n" +
                "`" + p + "prefixlist` - Shows all prefixes for users.\n" +
                "`" + p + "infolist` - Shows all info for startup.\n" +
                "`" + p + "modlist` - Shows all moderators.\n" +
                "`" + p + "filterlist` - Shows all chat filters.";
    }
}
