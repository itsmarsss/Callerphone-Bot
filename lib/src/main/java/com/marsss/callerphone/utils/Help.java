package com.marsss.callerphone.utils;

import java.awt.Color;

import com.marsss.callerphone.channelpool.commands.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import com.marsss.ICommand;
import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.bot.*;
import com.marsss.callerphone.tccallerphone.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Help implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        final String args[] = CONTENT.split("\\s+");

        boolean admin = Callerphone.admin.contains(e.getAuthor().getId());
        if (args.length > 1) {
            MESSAGE.replyEmbeds(help(args[1], admin)).queue();
            return;
        }

        MESSAGE.replyEmbeds(help("", admin)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent event) {

    }

    public static String getHelp() {
        return "`" + Callerphone.Prefix + "help` - help help help";
    }

    @Override
    public String getHelpF() {
        return "`" + Callerphone.Prefix + "help` - help help help";
    }

    @Override
    public String[] getTriggers() {
        return "help,gethelp,helpmeahhh".split(",");
    }

    public MessageEmbed help(String name, boolean admin) {
        if (name == "") {
            return helpCategories(admin);
        }

        String TITLE = "Sorry.";

        String DESC = "I don't recognize that category/command :(";
        name = name.toLowerCase();


        // Categories

        switch (name) {


            case "bot":
                TITLE = "Bot Commands";
                DESC = ICommand.getHelp() + "\n"
                        + Donate.getHelp() + "\n"
                        + Invite.getHelp() + "\n"
                        + Ping.getHelp() + "\n"
                        + Uptime.getHelp();
                break;


            case "utils":
                TITLE = "Util Commands";
                DESC = BotInfo.getHelp() + "\n"
                        + ChannelInfo.getHelp() + "\n"
                        + Colour.getHelp() + "\n"
                        + Help.getHelp() + "\n"
                        + RoleInfo.getHelp() + "\n"
                        + Search.getHelp() + "\n"
                        + ServerInfo.getHelp() + "\n"
                        + UserInfo.getHelp();
                break;


            case "pooling":
                TITLE = "Channel Pooling Commands";
                DESC = HostPool.getHelp() + "\n"
                        + JoinPool.getHelp() + "\n"
                        + EndPool.getHelp() + "\n"
                        + LeavePool.getHelp() + "\n"
                        + ParticipantsPool.getHelp() + "\n"
                        + PoolCap.getHelp() + "\n"
                        + PoolPub.getHelp() + "\n"
                        + PoolPwd.getHelp();
                break;


            case "tccall":
                TITLE = "TCCall Commands";
                DESC = TCCallPairer.callHelp() + "\n"
                        + TCCallPairer.uncenscallHelp() + "\n"
                        + TCCallPairer.hangupHelp() + "\n"
                        + TCCallPairer.reportHelp();
                break;


            case "report":
                TITLE = "Report Commands";
                DESC = TCCallPairer.reportHelp();
                break;


            case "music":
                TITLE = "Music Commands";
                DESC = "Callerphone no longer can play music, however I've created a new bot called **Tunes**...\nJoin [this](" + Callerphone.tunessupport + ") server for more information!";
                break;


        }


        if (Callerphone.cmdMap.containsKey(name)) {
            String[] triggers = Callerphone.cmdMap.get(name).getTriggers();
            String trigger = "";
            for (String trig : triggers) {
                trigger += trig + ", ";
            }

            TITLE = trigger.substring(0, trigger.length() - 2);
            DESC = Callerphone.cmdMap.get(name).getHelpF();

            if (trigger.contains("search")) {
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
