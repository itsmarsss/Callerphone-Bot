package com.itsmarsss.callerphone.bot;

import com.itsmarsss.ICommand;
import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.msginbottle.commands.FindBottle;
import com.itsmarsss.callerphone.msginbottle.commands.SendBottle;
import com.itsmarsss.callerphone.msginbottle.commands.ViewBottle;
import com.itsmarsss.callerphone.call.discord.CallCommand;
import com.itsmarsss.callerphone.call.discord.EndCallCommand;
import com.itsmarsss.callerphone.call.discord.PrefixCommand;
import com.itsmarsss.callerphone.call.discord.ReportCallCommand;
import com.itsmarsss.callerphone.discord.match.MatchCommand;
import com.itsmarsss.callerphone.minigames.commands.PlayMiniGame;
import com.itsmarsss.callerphone.minigames.commands.ShowMiniGames;
import com.itsmarsss.callerphone.users.commands.Leaderboard;
import com.itsmarsss.callerphone.users.commands.Profile;
import com.itsmarsss.commandType.ISlashCommand;
import com.itsmarsss.database.categories.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Help implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        boolean admin = Users.isModerator(e.getUser().getId());
        OptionMapping term = e.getOption("term");
        e.replyEmbeds(help(term != null ? term.getAsString() : "", admin)).queue();
    }

    public MessageEmbed help(String name, boolean admin) {
        if (name == null || name.isEmpty()) {
            return helpCategories(admin);
        }

        name = name.toLowerCase().trim();
        String title;
        String desc;

        switch (name) {
            case "bot" -> {
                title = "Bot";
                desc = joinHelp(new About(), new BotInfo(), new Donate(), new Help(), new Invite(),
                        new Profile(), new Leaderboard());
            }
            case "games", "minigames" -> {
                title = "Mini games";
                desc = joinHelp(new ShowMiniGames(), new PlayMiniGame());
            }
            case "tccall", "call" -> {
                title = "Call";
                desc = joinHelp(new CallCommand(), new EndCallCommand(), new ReportCallCommand(), new PrefixCommand())
                        + "\n\nChat with another server. Share your profile during a call if you want.";
            }
            case "match", "social" -> {
                title = "Match";
                desc = joinHelp(new MatchCommand())
                        + "\nDiscover people in your age group and start chats.";
            }
            case "msgbottle", "bottle", "bottles" -> {
                title = "Message in a bottle";
                desc = joinHelp(new SendBottle(), new FindBottle(), new ViewBottle());
            }
            case "creds", "credits" -> {
                title = "Credits";
                desc = "Earn credits as you use the bot.\n\n"
                        + "**Activity**\n"
                        + "· Message · `◉ 1`\n"
                        + "· Slash command · `◉ 2`\n"
                        + "· Call message · `◉ 5`\n\n"
                        + "**Other**\n"
                        + "· Bug report · `◉ 5,000`\n\n"
                        + "Call credits can be earned once every "
                        + (ToolSet.CREDIT_COOLDOWN / 1000) + " seconds.";
            }
            case "exp", "experience", "level" -> {
                title = "Experience";
                desc = "Each level needs **100** exp.\nCommands and call messages grant **1** exp each.";
            }
            default -> {
                ICommand cmd = Callerphone.cmdMap.get(name);
                if (cmd != null) {
                    title = capitalize(cmd.getName());
                    desc = cmd.getHelp();
                } else {
                    title = "Not found";
                    desc = "I don't recognize `" + name + "`.\nTry `/help` for categories.";
                }
            }
        }

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(ToolSet.COLOR)
                .setFooter("Callerphone · /help")
                .build();
    }

    private static String joinHelp(ICommand... commands) {
        return Arrays.stream(commands)
                .map(ICommand::getHelp)
                .collect(Collectors.joining("\n"));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private MessageEmbed helpCategories(boolean admin) {
        EmbedBuilder emb = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("Callerphone help")
                .setDescription("Pick a category, or pass a command name.")
                .addField("Match", "Discover people · `/help match`", false)
                .addField("Call", "Chat across servers · `/help call`", false)
                .addField("Message in a bottle", "Cast & find bottles · `/help msgbottle`", false)
                .addField("Mini games", "Play together · `/help games`", false)
                .addField("Bot", "Profile, invite, about · `/help bot`", false)
                .addField("Credits & exp", "`/help credits` · `/help exp`", false)
                .setFooter("Callerphone");
        if (admin) {
            emb.addField("Moderator", "DM `" + Callerphone.config.getPrefix() + "help mod`", false);
        }
        return emb.build();
    }

    @Override
    public String getHelp() {
        return "`/help` browse commands";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Browse help categories and commands")
                .addOptions(new OptionData(OptionType.STRING, "term", "Category or command name"));
    }
}
