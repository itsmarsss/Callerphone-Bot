package com.marsss.callerphone.utils;

import java.awt.Color;
import java.util.List;

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
        final String[] args = CONTENT.split("\\s+");

        boolean admin = Callerphone.admin.contains(e.getAuthor().getId());
        if (args.length > 1) {
            MESSAGE.replyEmbeds(help(args[1], admin)).queue();
            return;
        }

        MESSAGE.replyEmbeds(help("", admin)).queue();
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        boolean admin = Callerphone.admin.contains(e.getUser().getId());
        List<OptionMapping> param = e.getOptions();
        if(param.size() == 0) {
            e.replyEmbeds(help("", admin)).queue();
            return;
        }
        e.replyEmbeds(help(param.get(0).getAsString(), admin)).queue();
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
                DESC = new Donate().getHelp() + "\n"
                        + new Invite().getHelp() + "\n"
                        + new Ping().getHelp() + "\n"
                        + new Uptime().getHelp();
                break;


            case "utils":
                TITLE = "Util Commands";
                DESC = new BotInfo().getHelp() + "\n"
                        + new ChannelInfo().getHelp() + "\n"
                        + new Colour().getHelp() + "\n"
                        + new Help().getHelp() + "\n"
                        + new RoleInfo().getHelp() + "\n"
                        + new Search().getHelp() + "\n"
                        + new ServerInfo().getHelp() + "\n"
                        + new UserInfo().getHelp();
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
                        + new ReportChat().getHelp();
                break;


            case "music":
                TITLE = "Music Commands";
                DESC = "Callerphone no longer can play music, however I've created a new bot called **Tunes**...\nJoin [this](" + Callerphone.tunessupport + ") server for more information!";
                break;


        }


        if (Callerphone.cmdMap.containsKey(name)) {
            String[] triggers = Callerphone.cmdMap.get(name).getTriggers();
            StringBuilder trigger = new StringBuilder();
            for (String trig : triggers) {
                trigger.append(trig).append(", ");
            }

            TITLE = trigger.substring(0, trigger.length() - 2);
            DESC = Callerphone.cmdMap.get(name).getHelp();

            if (trigger.toString().contains("search")) {
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
