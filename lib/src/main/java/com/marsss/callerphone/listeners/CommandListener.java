package com.marsss.callerphone.listeners;

import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        final Message message = event.getMessage();
        if (message.isWebhookMessage())
            return;

        if (!event.getChannel().canTalk())
            return;

        final Member member = event.getMember();

        final String content = message.getContentRaw();

        final String[]args = content.split("\\s+");


        if (member.getUser().isBot() || member.getUser().isSystem())
            return;


        if (content.contains(Callerphone.jda.getSelfUser().getId())) {
            message.reply("My prefix is `" + Callerphone.Prefix + "`, do `" + Callerphone.Prefix + "help` for a list of commands!").queue();
            return;
        }

        if (!args[0].toLowerCase().startsWith(Callerphone.Prefix))
            return;

        if (args[0].toLowerCase().startsWith(Callerphone.Prefix + "play")) {
            message.reply("Callerphone no longer can play music, however I've created a new bot called **Tunes**...\nJoin <" + Callerphone.tunessupport + "> for more information!").queue();
            return;
        }

//        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
//            MESSAGE.reply("I need `Embed Links` permission to react.").queue();
//            return;
//        }

        String trigger = args[0].toLowerCase().replace(Callerphone.Prefix, "");

        if (Callerphone.cmdMap.containsKey(trigger)) {
            Callerphone.cmdMap.get(trigger).runCommand(event);
        }

    }

    public static String adminHelp() {
        return "`" + Callerphone.Prefix + "mod <id>` - Adds id to mod list.\n" +
                "`" + Callerphone.Prefix + "rmod <id>` - Removes id from mod list.";
    }

    public static String blacklistHelp() {
        return "`" + Callerphone.Prefix + "blacklist <id>` - Adds id to blacklist.\n" +
                "`" + Callerphone.Prefix + "rblacklist <id>` - Removes id from blacklist.";
    }

    public static String supportHelp() {
        return "`" + Callerphone.Prefix + "prefix <id> <prefix>` - Give user a prefix.\n" +
                "`" + Callerphone.Prefix + "rprefix <id>` - Removes user prefix.";
    }

    public static String showItemsHelp() {
        return "`" + Callerphone.Prefix + "blackedlist` - Shows all black listed users.\n" +
                "`" + Callerphone.Prefix + "prefixlist` - Shows all prefixes for users.\n" +
                "`" + Callerphone.Prefix + "infolist` - Shows all info for startup.\n" +
                "`" + Callerphone.Prefix + "modlist` - Shows all moderators.\n" +
                "`" + Callerphone.Prefix + "filterlist` - Shows all chat filters.";
    }

}
