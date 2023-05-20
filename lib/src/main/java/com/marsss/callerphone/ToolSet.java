package com.marsss.callerphone;

import com.marsss.callerphone.minigames.IMiniGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToolSet {
    public static String CP_EMJ = Callerphone.config.getCallerphoneNormal();
    public static String CP_ERR = Callerphone.config.getCallerphoneError();
    public static String CP_CALL = Callerphone.config.getCallerphoneCall();
    public static long MESSAGE_COOLDOWN = 500;
    public static long CREDIT_COOLDOWN = 15000;
    public static long COMMAND_COOLDOWN = 2000;
    public static long FINDBOTTLE_COOLDOWN = 43200000;
    public static long SENDBOTTLE_COOLDOWN = 17280000; // 86400000

    public static void updateToolSet() {
        CP_EMJ = Callerphone.config.getCallerphoneNormal();
        CP_ERR = Callerphone.config.getCallerphoneError();
        CP_CALL = Callerphone.config.getCallerphoneCall();
        MESSAGE_COOLDOWN = 500;
        CREDIT_COOLDOWN = 15000;
    }


    // https://programming.guide/java/formatting-byte-size-to-human-readable-format.html {

    public static String convert(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        final CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    // }

    public static TextChannel getTextChannel(String id) {
        if (id.isEmpty()) {
            return null;
        }

        long idL;
        try {
            idL = Long.parseLong(id);
        } catch (Exception e) {
            return null;
        }

        final TextChannel CHANNEL = Callerphone.jda.getTextChannelById(idL);

        if (CHANNEL == null)
            return null;

        if (!CHANNEL.getGuild().getSelfMember().hasPermission(CHANNEL, Permission.MESSAGE_SEND)) {
            return null;
        }

        return CHANNEL;
    }


    public static User getUser(String id) {
        if (id.isEmpty()) {
            return null;
        }

        long idL;
        try {
            idL = Long.parseLong(id);
        } catch (Exception e) {
            return null;
        }

        final User USER = Callerphone.jda.getUserById(idL);

        if (USER == null)
            return null;

        return USER;
    }


    public static String messageCheck(String messageRaw) {
        if (messageRaw.contains("@here") || messageRaw.contains("@everyone"))
            return Response.ATTEMPTED_PING.toString();

        if (hasLink(messageRaw))
            return Response.ATTEMPTED_LINK.toString();

        if (messageRaw.length() > 1500)
            return Response.MESSAGE_TOO_LONG.toString();

        return messageRaw;
    }

    public static boolean hasLink(String msg) {
        LinkedList<String> links = new LinkedList<>();
        String regexString = "((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)";
        Pattern pattern = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            links.add(msg.substring(matcher.start(0), matcher.end(0)));
        }

        return links.size() != 0;
    }

    public static String filter(String messageraw) {
        for (String ftr : Storage.filter) {
            StringBuilder rep = new StringBuilder();
            for (int i = 0; i < ftr.length(); i++) {
                rep.append("#");
            }
            messageraw = messageraw.replaceAll("(?i)" + ftr, rep.toString());
        }
        return messageraw;
    }

    public static void printWelcome() {
        System.out.println("  _____          _      _      ______ _____  _____  _    _  ____  _   _ ______ ");
        System.out.println(" / ____|   /\\   | |    | |    |  ____|  __ \\|  __ \\| |  | |/ __ \\| \\ | |  ____|");
        System.out.println("| |       /  \\  | |    | |    | |__  | |__) | |__) | |__| | |  | |  \\| | |__");
        System.out.println("| |      / /\\ \\ | |    | |    |  __| |  _  /|  ___/|  __  | |  | | . ` |  __|  ");
        System.out.println("| |____ / ____ \\| |____| |____| |____| | \\ \\| |    | |  | | |__| | |\\  | |____ ");
        System.out.println(" \\_____/_/    \\_\\______|______|______|_|  \\_\\_|    |_|  |_|\\____/|_| \\_|______|");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("               ============== PROGRAM SOURCE CODE =============");
        System.out.println("               = https://github.com/itsmarsss/Callerphone-Bot =");
        System.out.println("               ================================================");
        System.out.println("                    Welcome to Callerphone's Control Prompt");
    }

    public static void sendPPAndTOS(MessageReceivedEvent event) {
        event.getMessage().replyEmbeds(
                new EmbedBuilder()
                        .setAuthor("Must Read", null, event.getAuthor().getAvatarUrl())
                        .setTitle("User Agreement")
                        .setDescription("By issuing another Callerphone (**\"Bot\"**) command, it is expected that you (**\"User\"**) have read, and User has agreed to both Bot's [Privacy Policy](" + Callerphone.config.getPrivacyPolicy() + ") and [Terms of Service](" + Callerphone.config.getTermsOfService() + "). It is User's responsibility to regularly check for updates to these documents.")
                        .setFooter("This is to protect both Bot and User from unforeseen issues in the future. Please read these documents carefully.", Callerphone.jda.getSelfUser().getAvatarUrl())
                        .setColor(new Color(114, 137, 218))
                        .build()
        ).queue();
    }

    public static void sendPPAndTOS(SlashCommandInteractionEvent event) {
        event.replyEmbeds(
                new EmbedBuilder()
                        .setAuthor("Must Read", null, event.getUser().getAvatarUrl())
                        .setTitle("User Agreement")
                        .setDescription("By issuing another Callerphone (**\"Bot\"**) command, it is expected that you (**\"User\"**) have read, and User has agreed to both Bot's [Privacy Policy](" + Callerphone.config.getPrivacyPolicy() + ") and [Terms of Service](" + Callerphone.config.getTermsOfService() + "). It is User's responsibility to regularly check for updates to these documents.")
                        .setFooter("This is to protect both Bot and User from unforeseen issues in the future. Please read these documents carefully.", Callerphone.jda.getSelfUser().getAvatarUrl())
                        .setColor(new Color(114, 137, 218))
                        .build()
        ).queue();
    }

    public static void sendCommandCooldown(MessageReceivedEvent event) {
        event.getMessage().reply(":warning: **Command Cooldown;** " + ((ToolSet.COMMAND_COOLDOWN - (System.currentTimeMillis() - Storage.getCmdCooldown(event.getAuthor()))) / 1000) + " second(s)").queue();
    }

    public static void sendCommandCooldown(SlashCommandInteractionEvent event) {
        event.reply(":warning: **Command Cooldown;** " + ((ToolSet.COMMAND_COOLDOWN - (System.currentTimeMillis() - Storage.getCmdCooldown(event.getUser()))) / 1000) + " second(s)").queue();
    }

    public static void sendPrivateEmbed(User user, MessageEmbed embed) {
        user.openPrivateChannel().queue((channel) ->
                channel.sendMessageEmbeds(embed).queue()
        );
    }

    public static void sendPrivateMessage(User user, Message message) {
        user.openPrivateChannel().queue((channel) ->
                channel.sendMessage(MessageCreateData.fromMessage(message)).queue()
        );
    }

    public static void sendPrivateGameMessageFrom(User user, MessageCreateData message, IMiniGame game) {
        user.openPrivateChannel().queue((channel) -> {
                    game.setFromChannelId(channel.getId());
                    channel.sendMessage(message).queue((msg) -> {
                        game.setFromMessageId(msg.getId());
                    });
                }
        );
    }

    public static void sendPrivateGameMessageTo(User user, MessageCreateData message, IMiniGame game) {
        user.openPrivateChannel().queue((channel) -> {
                    game.setToChannelId(channel.getId());
                    channel.sendMessage(message).queue((msg) -> {
                        game.setToMessageId(msg.getId());
                    });
                }
        );
    }
}
