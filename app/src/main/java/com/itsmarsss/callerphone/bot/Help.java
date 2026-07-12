package com.itsmarsss.callerphone.bot;

import com.itsmarsss.ICommand;
import com.itsmarsss.callerphone.Callerphone;
import com.itsmarsss.callerphone.ToolSet;
import com.itsmarsss.callerphone.bootstrap.ApplicationContext;
import com.itsmarsss.callerphone.call.discord.CallCommand;
import com.itsmarsss.callerphone.call.discord.EndCallCommand;
import com.itsmarsss.callerphone.call.discord.PrefixCommand;
import com.itsmarsss.callerphone.call.discord.ReportCallCommand;
import com.itsmarsss.callerphone.discord.match.MatchCommand;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.minigames.commands.PlayMiniGame;
import com.itsmarsss.callerphone.minigames.commands.ShowMiniGames;
import com.itsmarsss.callerphone.msginbottle.commands.FindBottle;
import com.itsmarsss.callerphone.msginbottle.commands.SendBottle;
import com.itsmarsss.callerphone.msginbottle.commands.ViewBottle;
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

/**
 * Product home by default when Match is ready; pass a term for the command directory.
 * Category buttons edit the directory in place (plan patterns §28).
 */
public class Help implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        boolean admin = Users.isModerator(e.getUser().getId());
        OptionMapping term = e.getOption("term");
        if (term == null || term.getAsString().isBlank()) {
            replyHome(e, admin);
            return;
        }
        String name = term.getAsString().toLowerCase().trim();
        if ("home".equals(name) || "commands".equals(name) || "all".equals(name) || "directory".equals(name)) {
            try {
                if (ApplicationContext.isReady()) {
                    ApplicationContext.get().analytics().track(e.getUser().getId(), "help_open", "directory");
                }
            } catch (Exception ignored) {
            }
            e.reply(ExperienceRenderer.toMessage(HelpPresenter.directory(admin))).queue();
            return;
        }
        var embed = help(name, admin);
        String title = embed.getTitle() == null ? "Help" : embed.getTitle();
        String desc = embed.getDescription() == null ? "" : embed.getDescription();
        e.reply(ExperienceRenderer.toMessage(HelpPresenter.category(title, desc))).queue();
    }

    private void replyHome(SlashCommandInteractionEvent e, boolean admin) {
        if (!ApplicationContext.isReady()) {
            e.reply(ExperienceRenderer.toMessage(HelpPresenter.directory(admin))).queue();
            return;
        }
        // Personalized Match home + navigable command directory (edit-in-place categories)
        var home = MatchCommand.buildHome(ApplicationContext.get(), e.getUser().getId(), e.getUser().getName());
        try {
            ApplicationContext.get().analytics().track(e.getUser().getId(), "help_open", "home");
        } catch (Exception ignored) {
        }
        // Single product home; directory is one tap via /help commands
        e.reply(ExperienceRenderer.toMessage(home)).queue();
    }

    public MessageEmbed help(String name, boolean admin) {
        if (name == null || name.isEmpty()) {
            return ExperienceRenderer.toEmbed(HelpPresenter.directory(admin));
        }

        name = name.toLowerCase().trim();
        if ("home".equals(name) || "commands".equals(name) || "all".equals(name)) {
            return ExperienceRenderer.toEmbed(HelpPresenter.directory(admin));
        }

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
                desc = joinHelp(new ShowMiniGames(), new PlayMiniGame())
                        + "\n\nBest during a **Call** or Match chat — open **Play a game** there.";
            }
            case "tccall", "call" -> {
                title = "Call";
                desc = joinHelp(new CallCommand(), new EndCallCommand(), new ReportCallCommand(), new PrefixCommand())
                        + "\n\nOne queue for server channels and bot DMs — either side can pair. "
                        + "Share your Match profile during a call if you want.";
            }
            case "match", "social" -> {
                title = "Match";
                desc = joinHelp(new MatchCommand())
                        + "\nDiscover people your age, express interest, and chat when it's mutual.";
            }
            case "msgbottle", "bottle", "bottles" -> {
                title = "Message in a bottle";
                desc = joinHelp(new com.itsmarsss.callerphone.msginbottle.commands.BottleCommand(),
                        new SendBottle(), new FindBottle(), new ViewBottle())
                        + "\n\nSigned bottles offer **Interested** — same mutual-interest rules as Discover.";
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
                    desc = "I don't recognize `" + name + "`.\nTry `/help` for home, or `/help commands` for the directory.";
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

    @Override
    public String getHelp() {
        return "`/help` home · `/help commands` directory · category buttons edit in place";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Home screen and command directory")
                .addOptions(new OptionData(OptionType.STRING, "term", "Category or command name"));
    }
}
