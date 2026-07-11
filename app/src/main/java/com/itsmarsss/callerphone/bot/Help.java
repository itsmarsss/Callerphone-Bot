package com.itsmarsss.callerphone.bot;

import com.itsmarsss.ICommand;
import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.channelpool.commands.EndPool;
import com.itsmarsss.callerphone.channelpool.commands.HostPool;
import com.itsmarsss.callerphone.channelpool.commands.JoinPool;
import com.itsmarsss.callerphone.channelpool.commands.KickPool;
import com.itsmarsss.callerphone.channelpool.commands.LeavePool;
import com.itsmarsss.callerphone.channelpool.commands.PoolParticipants;
import com.itsmarsss.callerphone.channelpool.commands.PoolSettings;
import com.itsmarsss.callerphone.msginbottle.commands.FindBottle;
import com.itsmarsss.callerphone.msginbottle.commands.SendBottle;
import com.itsmarsss.callerphone.msginbottle.commands.ViewBottle;
import com.itsmarsss.callerphone.call.discord.CallCommand;
import com.itsmarsss.callerphone.call.discord.EndCallCommand;
import com.itsmarsss.callerphone.call.discord.ReportCallCommand;
import com.itsmarsss.callerphone.discord.match.MatchCommand;
import com.itsmarsss.callerphone.tccallerphone.commands.Prefix;
import com.itsmarsss.callerphone.minigames.commands.PlayMiniGame;
import com.itsmarsss.callerphone.minigames.commands.ShowMiniGames;
import com.itsmarsss.callerphone.users.commands.Leaderboard;
import com.itsmarsss.callerphone.users.commands.Profile;
import com.itsmarsss.callerphone.utils.ChannelInfo;
import com.itsmarsss.callerphone.utils.Colour;
import com.itsmarsss.callerphone.utils.RoleInfo;
import com.itsmarsss.callerphone.utils.Search;
import com.itsmarsss.callerphone.utils.ServerInfo;
import com.itsmarsss.callerphone.utils.UserInfo;
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
        String title = "Sorry.";
        String desc = "I don't recognize that category/command :(";

        switch (name) {
            case "bot":
                title = "Bot Commands";
                desc = joinHelp(new About(), new BotInfo(), new Donate(), new Help(), new Invite(),
                        new Profile(), new Leaderboard());
                break;
            case "games":
            case "minigames":
                title = "MiniGames";
                desc = joinHelp(new ShowMiniGames(), new PlayMiniGame());
                break;
            case "utils":
                title = "Util Commands";
                desc = joinHelp(new ServerInfo(), new ChannelInfo(), new RoleInfo(), new UserInfo(), new Colour(), new Search());
                break;
            case "pooling":
                title = "Channel Pooling Commands";
                desc = joinHelp(new HostPool(), new JoinPool(), new EndPool(), new LeavePool(),
                        new KickPool(), new PoolParticipants(), new PoolSettings());
                break;
            case "tccall":
            case "call":
                title = "Random Call";
                desc = joinHelp(new CallCommand(), new EndCallCommand(), new ReportCallCommand(), new Prefix())
                        + "\n\nSingle mode only (anon / family-friendly removed). "
                        + "Share Match profiles during a call to like each other.";
                break;
            case "match":
            case "social":
                title = "Callerphone Social (Match)";
                desc = joinHelp(new MatchCommand());
                break;
            case "msgbottle":
                title = "Message In Bottle";
                desc = joinHelp(new SendBottle(), new FindBottle(), new ViewBottle());
                break;
            case "music":
                title = "Music Commands";
                desc = "Callerphone no longer can play music, however I've created a new bot called **Tunes**...\nJoin [this]("
                        + Callerphone.config.getSupportServer() + ") server for more information!";
                break;
            case "creds":
                title = "**EARN CREDITS**";
                desc = "__Commands:__" +
                        "\n> Message ~ `\u23E3 1`" +
                        "\n> Slash ~ `\u23E3 2`" +
                        "\n\n__Messages:__" +
                        "\n> Channel Pool ~ `\u23E3 3`" +
                        "\n> Channel Chat ~ `\u23E3 5`" +
                        "\n\n__Other:__" +
                        "\n> Bug Report ~ `\u23E3 5,000`" +
                        "\n\n**NOTE:** Channel Pool/Chat can be earned a maximum of once per "
                        + (ToolSet.CREDIT_COOLDOWN / 1000) + " seconds. *(Spam prevention)*";
                break;
            case "exp":
                title = "**EARN EXPERIENCE**";
                desc = "__**Temporary:**__" +
                        "\n> Each level requires 100 exp, and each command/message transferred is worth 1 exp.";
                break;
            default:
                ICommand cmd = Callerphone.cmdMap.get(name);
                if (cmd != null) {
                    title = cmd.getName();
                    desc = cmd.getHelp();
                    if ("search".equals(name)) {
                        desc += "\nWe use DuckDuckGo, so click [here](https://help.duckduckgo.com/duckduckgo-help-pages/results/syntax/) for searching syntax!";
                    }
                }
                break;
        }

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setFooter("Hope you found this useful!",
                        Callerphone.selfUser != null ? Callerphone.selfUser.getAvatarUrl() : null)
                .setColor(ToolSet.COLOR)
                .build();
    }

    private static String joinHelp(ICommand... commands) {
        return Arrays.stream(commands)
                .map(ICommand::getHelp)
                .collect(Collectors.joining("\n"));
    }

    private MessageEmbed helpCategories(boolean admin) {
        EmbedBuilder categoryEmbed = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("Categories")
                .addField("Bot", "Bot commands — `/help bot`", false)
                .addField("Utils", "Utility commands — `/help utils`", false)
                .addField("Pooling", "Channel pooling — `/help pooling`", false)
                .addField("Random Call", "Cross-server chat — `/help call`", false)
                .addField("Match / Social", "Discover & connect — `/help match`", false)
                .addField("Msg Bottles", "Message in bottle — `/help msgbottle`", false)
                .addField("MiniGames", "Playable games — `/help games`", false)
                .addField("Music", "Callerphone no longer plays music", false)
                .setFooter("Type `/help <category name>` to see category commands");
        if (admin) {
            categoryEmbed.addField("Moderator only",
                    "Moderator commands — `" + Callerphone.config.getPrefix() + "help mod` in DM", false);
        }
        return categoryEmbed.build();
    }

    @Override
    public String getHelp() {
        return "</help:1075169172423720970> - help help help";
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
