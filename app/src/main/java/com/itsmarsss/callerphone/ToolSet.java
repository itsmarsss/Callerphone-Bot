package com.itsmarsss.callerphone;

import com.itsmarsss.callerphone.minigames.IMiniGame;
import com.itsmarsss.database.categories.Cooldown;
import com.itsmarsss.database.categories.Filter;
import com.itsmarsss.database.categories.Users;
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class ToolSet {
    public static String CP_EMJ = "";
    public static String CP_ERR = "";
    public static String CP_CALL = "";
    public static long MESSAGE_COOLDOWN = Constants.MESSAGE_COOLDOWN;
    public static long CREDIT_COOLDOWN = Constants.CREDIT_COOLDOWN;
    public static long COMMAND_COOLDOWN = Constants.COMMAND_COOLDOWN;
    public static long FINDBOTTLE_COOLDOWN = Constants.FINDBOTTLE_COOLDOWN;
    public static long SENDBOTTLE_COOLDOWN = Constants.SENDBOTTLE_COOLDOWN;
    public static Color COLOR = new Color(114, 137, 218);

    private static final Pattern LINK_PATTERN = Pattern.compile(
            "((http://|https://)?(www\\.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(/([a-zA-Z-_/.0-9#:?=&;,]*)?)?)",
            Pattern.CASE_INSENSITIVE
    );

    private ToolSet() {
    }

    public static void updateToolSet() {
        if (Callerphone.config == null) {
            return;
        }
        CP_EMJ = Callerphone.config.getCallerphoneNormal();
        CP_ERR = Callerphone.config.getCallerphoneError();
        CP_CALL = Callerphone.config.getCallerphoneCall();
    }

    public static String generateUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static TextChannel getTextChannel(String id) {
        if (id == null || id.isEmpty() || Callerphone.sdMgr == null) {
            return null;
        }
        try {
            TextChannel channel = Callerphone.sdMgr.getTextChannelById(id);
            if (channel == null) {
                return null;
            }
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)) {
                return null;
            }
            return channel;
        } catch (Exception e) {
            return null;
        }
    }

    public static RestAction<User> getUser(String id) {
        if (id == null || id.isEmpty() || Callerphone.sdMgr == null) {
            return null;
        }
        try {
            return Callerphone.sdMgr.retrieveUserById(id);
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] messageFlagged(String messageRaw) {
        if (messageRaw == null) {
            return new String[0];
        }
        List<String> flagged = new ArrayList<>(4);
        if (hasPing(messageRaw)) {
            flagged.add("ping");
        }
        if (hasLink(messageRaw)) {
            flagged.add("link");
        }
        if (messageRaw.length() > Constants.MAX_MESSAGE_LENGTH) {
            flagged.add("length");
        }
        if (containsProfanity(messageRaw)) {
            flagged.add("profanity");
        }
        return flagged.toArray(new String[0]);
    }

    public static String filterMessage(String messageRaw) {
        if (messageRaw == null) {
            return "";
        }
        if (hasPing(messageRaw)) {
            return Response.ATTEMPTED_PING.toString();
        }
        if (hasLink(messageRaw)) {
            return Response.ATTEMPTED_LINK.toString();
        }
        if (messageRaw.length() > Constants.MAX_MESSAGE_LENGTH) {
            return Response.MESSAGE_TOO_LONG.toString();
        }

        String filtered = messageRaw;
        for (Pattern pattern : Filter.getContainsPatterns()) {
            filtered = pattern.matcher(filtered).replaceAll(match -> Filter.censor(match.group()));
        }
        for (Pattern pattern : Filter.getWordPatterns()) {
            filtered = pattern.matcher(filtered).replaceAll(match -> " " + Filter.censor(match.group()) + " ");
        }
        return filtered;
    }

    public static boolean containsProfanity(String messageRaw) {
        if (messageRaw == null || messageRaw.isEmpty()) {
            return false;
        }
        for (Pattern pattern : Filter.getContainsPatterns()) {
            if (pattern.matcher(messageRaw).find()) {
                return true;
            }
        }
        for (Pattern pattern : Filter.getWordPatterns()) {
            if (pattern.matcher(messageRaw).find()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPing(String msg) {
        return msg != null && (msg.contains("@here") || msg.contains("@everyone"));
    }

    public static boolean hasLink(String msg) {
        return msg != null && LINK_PATTERN.matcher(msg).find();
    }

    /** @deprecated use {@link Filter#buildSpacedWordRegex(String)} */
    @Deprecated
    public static String generateRegex(String word) {
        return Filter.buildSpacedWordRegex(word);
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
                .setFooter("This is to protect both Bot and User from unforeseen issues in the future. Please read these documents carefully.",
                        Callerphone.selfUser != null ? Callerphone.selfUser.getAvatarUrl() : null)
                .setColor(COLOR)
                .build();
    }

    public static void sendCommandCooldown(SlashCommandInteractionEvent event) {
        long remainingMs = COMMAND_COOLDOWN - (System.currentTimeMillis() - Cooldown.getCmdCooldown(event.getUser().getId()));
        long remainingSec = Math.max(1, remainingMs / 1000);
        event.reply(":warning: **Command Cooldown;** " + remainingSec + " second(s)")
                .setEphemeral(true).queue();
    }

    public static String formatCooldown(long elapsedMs, long cooldownMs, String unitSecondsLabel) {
        if (elapsedMs >= cooldownMs) {
            return ":white_check_mark: None";
        }
        long remaining = Math.max(1, (cooldownMs - elapsedMs) / 1000);
        return ":alarm_clock: " + remaining + " " + unitSecondsLabel;
    }

    public static void sendPrivateEmbed(User user, MessageEmbed embed) {
        user.openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(embed).queue());
    }

    public static void sendPrivateMessage(User user, Message message) {
        user.openPrivateChannel().queue(channel ->
                channel.sendMessage(MessageCreateData.fromMessage(message)).queue());
    }

    public static void sendPrivateGameMessageFrom(User user, MessageCreateData message, IMiniGame game) {
        user.openPrivateChannel().queue(channel -> {
            game.setFromChannelId(channel.getId());
            channel.sendMessage(message).queue(msg -> game.setFromMessageId(msg.getId()));
        });
    }

    public static void sendPrivateGameMessageTo(User user, MessageCreateData message, IMiniGame game) {
        user.openPrivateChannel().queue(channel -> {
            game.setToChannelId(channel.getId());
            channel.sendMessage(message).queue(msg -> game.setToMessageId(msg.getId()));
        });
    }
}
