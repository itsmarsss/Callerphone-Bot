package com.marsss.callerphone.utils;

import java.awt.Color;
import java.io.IOException;

import com.marsss.ICommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.marsss.callerphone.Callerphone;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Search implements ICommand {

    @Override
    public void runCommand(GuildMessageReceivedEvent e) {
        final Message MESSAGE = e.getMessage();
        final String CONTENT = MESSAGE.getContentRaw();
        if (CONTENT.substring(8).equals("")) {
            MESSAGE.reply("Please enter a search query!").queue();
            return;
        }

        try {
            MESSAGE.replyEmbeds(search(CONTENT.substring(8))).queue();
        } catch (IOException e1) {
            e1.printStackTrace();
            MESSAGE.reply("Error getting links").queue();
        }
    }

    @Override
    public void runSlash(SlashCommandEvent e) {
        try {
            e.replyEmbeds(search(e.getOption("query").getAsString())).queue();
        } catch (IOException e1) {
            e1.printStackTrace();
            e.reply("Error getting links").queue();
        }
    }

    public MessageEmbed search(String query) throws IOException {
        query = query.substring(1);

        final String url = "https://www.duckduckgo.com/html" + "?q=" + query;

        final Document doc = Jsoup.connect(url).get();

        final Elements links = doc.getElementById("links").getElementsByClass("results_links");

        final Color COLOR = Colour.randColor();

        EmbedBuilder GglEmd = new EmbedBuilder()
                .setColor(COLOR)
                .setTitle("Search Results for *" + query + "*", url.replaceAll("\\s+", "%20"));

        for (int i = 0; i < 3; i++) {
            try {
                final Element currlink = links.get(i).getElementsByClass("links_main").first().getElementsByTag("a").first();

                String title = currlink.text();
                String snippet = links.get(i).getElementsByClass("result__snippet").first().text();
                String hyper = currlink.attr("href");

                if (title.length() > 100) {
                    title = title.substring(0, 197) + "...";
                }

                if (snippet.length() > 1000) {
                    snippet = snippet.substring(0, 1997) + "...";
                }

                GglEmd.addField("__" + title + "__",
                        snippet +
                                "\n[[Link]](" + hyper + ")", false);
            } catch (Exception e) {
            }

        }


        return GglEmd.build();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.Prefix + "search <query>` - Search for something quickly on the web with title, snippet, and link!";
    }

    @Override
    public String[] getTriggers() {
        return "search,ddg,query".split(",");
    }

}
