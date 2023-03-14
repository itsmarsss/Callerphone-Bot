package com.marsss.callerphone.listeners;

import java.awt.Color;
import java.io.File;

import com.marsss.callerphone.Callerphone;

import com.marsss.callerphone.Storage;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.AttachmentOption;

// IF IT WORKS DON'T TOUCH IT
// Too bad, I have to fix it
public class OnPrivateMessage extends ListenerAdapter {
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
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

                switch (args[0].toLowerCase().replace(Callerphone.config.getPrefix(), "")) {

                    case "filters":
                        ToolSet.sendPrivateFile(MEMBER, new File(Callerphone.parent + "/filter.txt"), "Callerphone Filter list:");
                        return;

                    case "users":
                        ToolSet.sendPrivateFile(MEMBER, new File(Callerphone.parent + "/users.json"), "Callerphone Filter list:");
                        return;

                    case "pools":
                        ToolSet.sendPrivateFile(MEMBER, new File(Callerphone.parent + "/pools.json"), "Callerphone Filter list:");
                        return;

                }

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
}
