package com.marsss.callerphone.utils;

import java.awt.Color;
import java.util.List;

import com.marsss.callerphone.ToolSet;
import com.marsss.callerphone.channelpool.commands.*;
import com.marsss.callerphone.tccallerphone.commands.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.bot.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Help implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        final String[] ARGS = CONTENT.split("\\s+");

        boolean admin = Callerphone.admin.contains(e.getAuthor().getId());
        if (ARGS.length > 1) {
            MESSAGE.replyEmbeds(help(ARGS[1], admin)).queue();
            return;
        }

        MESSAGE.replyEmbeds(help("", admin)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        final boolean ADMIN = Callerphone.admin.contains(e.getUser().getId());
        final List<OptionMapping> PARAM = e.getOptions();
        if (PARAM.size() == 0) {
            e.replyEmbeds(help("", ADMIN)).queue();
            return;
        }
        e.replyEmbeds(help(PARAM.get(0).getAsString(), ADMIN)).queue();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "help` - help help help";
    }

    @Override
    public String[] getTriggers() {
        return "help,gethelp,helpmeahhh".split(",");
    }

    public MessageEmbed help(String name, boolean admin) {
        if (name.equals("")) {
            return helpCategories(admin);
        }

        String TITLE = "Sorry.";

        String DESC = "I don't recognize that category/command :(";
        name = name.toLowerCase();


        // Categories

        switch (name) {


            case "bot":
                TITLE = "Bot Commands";
                DESC = new About().getHelp() + "\n"
                        + new BotInfo().getHelp() + "\n"
                        + new Donate().getHelp() + "\n"
                        + new Invite().getHelp() + "\n"
                        + new Ping().getHelp() + "\n"
                        + new Profile().getHelp() + "\n"
                        + new Uptime().getHelp();
                break;


            case "utils":
                TITLE = "Util Commands";
//                DESC = new BotInfo().getHelp() + "\n"
//                        + new ChannelInfo().getHelp() + "\n"
//                        + new Colour().getHelp() + "\n"
//                        + new Help().getHelp() + "\n"
//                        + new RoleInfo().getHelp() + "\n"
//                        + new Search().getHelp() + "\n"
//                        + new ServerInfo().getHelp() + "\n"
//                        + new UserInfo().getHelp();

                DESC = new BotInfo().getHelp() + "\n"
                        + new Colour().getHelp() + "\n"
                        + new Help().getHelp();
                break;


            case "pooling":
                TITLE = "Channel Pooling Commands";
                DESC = new HostPool().getHelp() + "\n"
                        + new JoinPool().getHelp() + "\n"
                        + new EndPool().getHelp() + "\n"
                        + new LeavePool().getHelp() + "\n"
                        + new PoolParticipants().getHelp() + "\n"
                        + new PoolCap().getHelp() + "\n"
                        + new PoolPub().getHelp() + "\n"
                        + new PoolPwd().getHelp() + "\n"
                        + new PoolKick().getHelp();
                break;


            case "tccall":
                TITLE = "TCCall Commands";
                DESC = new Chat().getHelp() + "\n"
                        + new EndChat().getHelp() + "\n"
                        + new ReportChat().getHelp() + "\n"
                        + new Prefix();
                break;


            case "music":
                TITLE = "Music Commands";
                DESC = "Callerphone no longer can play music, however I've created a new bot called **Tunes**...\nJoin [this](" + Callerphone.tunessupport + ") server for more information!";
                break;


            case "creds":
                TITLE = "**EARN CREDITS**";
                DESC = "__Commands:__" +
                        "\n> Message ~ `\u00A9 1`" +
                        "\n> Slash ~ `\u00A9 2`" +
                        "\n\n__Messages:__" +
                        "\n> Channel Pool ~ `\u00A9 3`" +
                        "\n> Channel Chat ~ `\u00A9 5`" +
                        "\n\n__Other:__" +
                        "\n> Bug Report ~ `\u00A9 5,000`" +
                "\n\n**NOTE:** Channel Pool/Chat can be earned a maximum of once per " + (ToolSet.CREDIT_COOLDOWN/1000) + " seconds. *(Spam prevention)*";
                break;

            case "exp":
                TITLE = "**EARN EXPERIENCE**";
                DESC = "__**Temporary:**__" +
                        "\n> Each level required 100 exp, and each command/message transferred are worth 1 exp.";
                break;


        }


        if (Callerphone.cmdMap.containsKey(name)) {
            final String[] TRIGGERS = Callerphone.cmdMap.get(name).getTriggers();
            final StringBuilder TRIGGER = new StringBuilder();
            for (String trig : TRIGGERS) {
                TRIGGER.append(trig).append(", ");
            }

            TITLE = TRIGGER.substring(0, TRIGGER.length() - 2);
            DESC = Callerphone.cmdMap.get(name).getHelp();

            if (TRIGGER.toString().contains("search")) {
                DESC += "\nWe use Duckduckgo, so click [here](https://help.duckduckgo.com/duckduckgo-help-pages/results/syntax/) for searching syntax!";
            }
        }

        EmbedBuilder HelpEmd = new EmbedBuilder()
                .setTitle(TITLE)
                .setDescription(DESC)
                .setFooter("Hope you found this useful!", Callerphone.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(114, 137, 218));

        return HelpEmd.build();
    }

    private MessageEmbed helpCategories(boolean admin) {
        EmbedBuilder CateEmd = new EmbedBuilder()
                .setColor(new Color(114, 137, 218))
                .setTitle("Categories")
                .addField("Bot", "all commands related to the bot will be here, do `" + Callerphone.Prefix + "help bot` for more information", false)
                .addField("Utils", "all utility commands will be in this category, do `" + Callerphone.Prefix + "help utils` for more information", false)
                .addField("Pooling", "all channel pooling commands will be in this category, do `" + Callerphone.Prefix + "help pooling` for more information", false)
                .addField("TC Callerphone", "all text call callerphone commands will be in this category, do `" + Callerphone.Prefix + "help tccall` for more information", false)
                .addField("Music", "Callerphone no longer can play music, however I've created a new bot called **Tunes**... Join [this](https:discord.gg/TyHaxtWAmX) server for more information!", false)
                .setFooter("Type `" + Callerphone.Prefix + "help <category name>` to see category commands");
        if (admin) {
            CateEmd.addField("Moderator only", "all moderator commands will be in this category, do `" + Callerphone.Prefix + "help mod` in dm for more information", false);
        }
        return CateEmd.build();
    }

}
