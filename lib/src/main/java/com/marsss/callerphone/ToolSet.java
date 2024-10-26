package com.marsss.callerphone;

import com.marsss.callerphone.minigames.IMiniGame;
import com.marsss.database.categories.Cooldown;
import com.marsss.database.categories.Filter;
import com.marsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.LinkedList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToolSet {
    public static String CP_EMJ = Callerphone.config.getCallerphoneNormal();
    public static String CP_ERR = Callerphone.config.getCallerphoneError();
    public static String CP_CALL = Callerphone.config.getCallerphoneCall();
    public static long MESSAGE_COOLDOWN = 500;
    public static long CREDIT_COOLDOWN = 15000;
    public static long COMMAND_COOLDOWN = 3000;
    public static long FINDBOTTLE_COOLDOWN = 43200000;
    public static long SENDBOTTLE_COOLDOWN = 17280000; // 86400000
    public static Color COLOR = new Color(114, 137, 218);

    public static void updateToolSet() {
        CP_EMJ = Callerphone.config.getCallerphoneNormal();
        CP_ERR = Callerphone.config.getCallerphoneError();
        CP_CALL = Callerphone.config.getCallerphoneCall();
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

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

        final TextChannel CHANNEL = Callerphone.sdMgr.getTextChannelById(idL);

        if (CHANNEL == null)
            return null;

        if (!CHANNEL.getGuild().getSelfMember().hasPermission(CHANNEL, Permission.MESSAGE_SEND)) {
            return null;
        }

        return CHANNEL;
    }


    public static RestAction<User> getUser(String id) {
        if (id.isEmpty()) {
            return null;
        }

        long idL;
        try {
            idL = Long.parseLong(id);
        } catch (Exception e) {
            return null;
        }

        return Callerphone.sdMgr.retrieveUserById(idL);
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
        for (String ftr : Filter.filter) {
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
        event.getMessage().replyEmbeds(buildPPAndTOS(event.getAuthor())).queue();
        Users.createUser(event.getAuthor().getId());
    }

    public static void sendPPAndTOS(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildPPAndTOS(event.getUser())).queue();
        Users.createUser(event.getUser().getId());
    }

    public static MessageEmbed buildPPAndTOS(User user) {
        return new EmbedBuilder()
                .setAuthor("Must Read", null, user.getAvatarUrl())
                .setTitle("User Agreement")
                .setDescription("By issuing another Callerphone (**\"Bot\"**) command or message in the Bot's scope, it is expected that you (**\"User\"**) have read, and User has agreed to both Bot's [Privacy Policy](" + Callerphone.config.getPrivacyPolicy() + ") and [Terms of Service](" + Callerphone.config.getTermsOfService() + "). It is User's responsibility to regularly check for updates to these documents.")
                .setFooter("This is to protect both Bot and User from unforeseen issues in the future. Please read these documents carefully.", Callerphone.selfUser.getAvatarUrl())
                .setColor(ToolSet.COLOR)
                .build();
    }

    public static void sendCommandCooldown(SlashCommandInteractionEvent event) {
        event.reply(":warning: **Command Cooldown;** " + ((ToolSet.COMMAND_COOLDOWN - (System.currentTimeMillis() - Cooldown.getCmdCooldown(event.getUser().getId()))) / 1000) + " second(s)").queue();
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
                    channel.sendMessage(message).queue((msg) -> game.setFromMessageId(msg.getId()));
                }
        );
    }

    public static void sendPrivateGameMessageTo(User user, MessageCreateData message, IMiniGame game) {
        user.openPrivateChannel().queue((channel) -> {
                    game.setToChannelId(channel.getId());
                    channel.sendMessage(message).queue((msg) -> game.setToMessageId(msg.getId()));
                }
        );
    }
}
