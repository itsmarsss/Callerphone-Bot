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
import com.itsmarsss.callerphone.discord.match.MatchPresenter;
import com.itsmarsss.callerphone.experience.ExperienceRenderer;
import com.itsmarsss.callerphone.match.model.MatchConversation;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;
import com.itsmarsss.callerphone.match.service.ProfileChecklist;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Product home by default; pass a term for the command directory.
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
        e.replyEmbeds(help(term.getAsString(), admin)).queue();
    }

    private void replyHome(SlashCommandInteractionEvent e, boolean admin) {
        if (!ApplicationContext.isReady()) {
            e.replyEmbeds(directoryEmbed(admin)).queue();
            return;
        }
        ApplicationContext ctx = ApplicationContext.get();
        String userId = e.getUser().getId();
        var user = ctx.enrollment().getOrCreate(userId);
        Optional<MatchProfile> profile = ctx.profiles().find(userId);
        boolean enrolled = user.isEnrolled();
        boolean live = profile.isPresent()
                && profile.get().getState() == ProfileState.ACTIVE
                && ProfileChecklist.readyToSubmit(profile.get());
        String name = profile.map(MatchProfile::getDisplayName).orElse(e.getUser().getName());
        int unread = 0;
        long left = 0;
        if (live) {
            MatchProfile p = profile.get();
            ctx.profiles().resetDailyCountersIfNeeded(p);
            left = Math.max(0, ctx.premium().dailyDiscoveries(userId) - p.getDiscoveryViewsToday());
            for (MatchConversation chat : ctx.conversations().list(userId)) {
                unread += chat.unreadFor(userId);
            }
        }
        e.reply(ExperienceRenderer.toMessage(MatchPresenter.home(name, unread, left, live, enrolled)))
                .addEmbeds(directoryEmbed(admin))
                .queue();
    }

    public MessageEmbed help(String name, boolean admin) {
        if (name == null || name.isEmpty()) {
            return directoryEmbed(admin);
        }

        name = name.toLowerCase().trim();
        if ("home".equals(name) || "commands".equals(name) || "all".equals(name)) {
            return directoryEmbed(admin);
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
                desc = joinHelp(new ShowMiniGames(), new PlayMiniGame());
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
                        new SendBottle(), new FindBottle(), new ViewBottle());
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

    private MessageEmbed directoryEmbed(boolean admin) {
        EmbedBuilder emb = new EmbedBuilder()
                .setColor(ToolSet.COLOR)
                .setTitle("All commands")
                .setDescription("Shortcuts if you prefer slash commands over buttons.")
                .addField("Match", "Discover people · `/help match`", false)
                .addField("Call", "Server or DM · `/help call`", false)
                .addField("Bottles", "`/bottle send|find|saved` · `/help bottle`", false)
                .addField("Mini games", "Best during a call · `/help games`", false)
                .addField("Bot", "Profile, invite, about · `/help bot`", false)
                .setFooter("Callerphone");
        if (admin) {
            emb.addField("Moderator", "DM `" + Callerphone.config.getPrefix() + "help mod`", false);
        }
        return emb.build();
    }

    @Override
    public String getHelp() {
        return "`/help` home · `/help commands` directory";
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
