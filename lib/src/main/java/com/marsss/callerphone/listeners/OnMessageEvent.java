package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import com.marsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnMessageEvent extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild())
            return;

        if (!Users.isModerator(event.getAuthor().getId()))
            return;

        if (event.getAuthor().isBot() || event.getAuthor().isSystem())
            return;

        final User MEMBER = event.getAuthor();
        final Message MESSAGE = event.getMessage();

        String CONTENT = MESSAGE.getContentRaw();

        final String[] args = CONTENT.split("\\s+");

        if (CONTENT.startsWith(Callerphone.config.getPrefix() + "help mod")) {
            String TITLE = "Mod";
            String DESC = OnMessageEvent.adminHelp() + "\n"
                    + OnMessageEvent.blacklistHelp() + "\n"
                    + OnMessageEvent.supportHelp() + "\n"
                    + OnMessageEvent.showItemsHelp();

            EmbedBuilder HelpEmd = new EmbedBuilder()
                    .setTitle(TITLE)
                    .setDescription(DESC)
                    .setFooter("Hope you found this useful!", Callerphone.selfUser.getAvatarUrl())
                    .setColor(ToolSet.COLOR);

            ToolSet.sendPrivateEmbed(MEMBER, HelpEmd.build());
            return;
        }

        if (CONTENT.toLowerCase().startsWith(Callerphone.config.getPrefix())) {
            try {
                String id = args[1];
                switch (args[0].toLowerCase().replace(Callerphone.config.getPrefix(), "")) {

                    case "blacklist":
                        if (Users.isBlacklisted(id)) {
                            MESSAGE.reply("ID blacklisted already").queue();
                        } else {
                            Users.addBlacklist(id);
                            MESSAGE.reply("ID: `" + id + "` added to blacklist").queue();
                        }
                        break;

                    case "prefix":
                        if (Users.hasPrefix(id)) {
                            MESSAGE.reply("ID has prefix already (" + Users.getPrefix(id) + ")").queue();
                        } else {
                            String prefix = args[2];
                            if (prefix.length() > 15) {
                                MESSAGE.reply("Prefix too long (max. length is 15 chars)").queue();
                                break;
                            }
                            Users.setPrefix(id, prefix);
                            MESSAGE.reply("ID: `" + id + "` now has prefix `" + prefix + "`").queue();
                        }
                        break;

                    case "mod":
                        if (Users.isModerator(id)) {
                            MESSAGE.reply("ID is mod already").queue();
                        } else {
                            Users.addModerator(id);
                            MESSAGE.reply("ID: `" + id + "` added to mod list").queue();
                        }
                        break;


                    case "rblacklist":
                        if (!Users.isBlacklisted(id)) {
                            MESSAGE.reply("ID not blacklisted").queue();
                        } else {
                            Users.addUser(id);
                            MESSAGE.reply("ID: `" + id + "` removed from blacklist").queue();
                        }
                        break;

                    case "rprefix":
                        if (!Users.hasPrefix(id)) {
                            MESSAGE.reply("ID does not have a prefix").queue();
                        } else {
                            Users.setPrefix(id, "");
                            MESSAGE.reply("ID: `" + id + "` no longer has a prefix").queue();
                        }
                        break;

                    case "rmod":
                        if (!Users.isModerator(id)) {
                            MESSAGE.reply("ID is not a mod").queue();
                        } else {
                            if (id.equals(Callerphone.config.getOwnerID())) {
                                MESSAGE.reply("You cannot remove this mod").queue();
                                break;
                            }
                            Users.addUser(id);
                            MESSAGE.reply("ID: `" + id + "` removed from mod list").queue();
                        }
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
                sendError(MESSAGE, e);
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
