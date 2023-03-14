package com.marsss.callerphone.dormant;

import com.marsss.callerphone.Callerphone;
import com.marsss.callerphone.utils.Colour;
import com.marsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;

public class Search implements ISlashCommand {
    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        try {
            e.replyEmbeds(search(e.getOption("query").getAsString())).queue();
        } catch (IOException e1) {
            e1.printStackTrace();
            e.reply("Error getting links").queue();
        }
    }

    public MessageEmbed search(String query) throws IOException {
        query = query.substring(1);

        final String URL = "https://www.duckduckgo.com/html" + "?q=" + query;

        final Document DOC = Jsoup.connect(URL).get();

        final Elements LINKS = DOC.getElementById("links").getElementsByClass("results_links");

        final Color COLOR = Colour.randColor();

        EmbedBuilder GglEmd = new EmbedBuilder()
                .setColor(COLOR)
                .setTitle("Search Results for *" + query + "*", URL.replaceAll("\\s+", "%20"));

        for (int i = 0; i < 3; i++) {
            try {
                final Element CURRENT_LINK = LINKS.get(i).getElementsByClass("links_main").first().getElementsByTag("a").first();

                String title = CURRENT_LINK.text();
                String snippet = LINKS.get(i).getElementsByClass("result__snippet").first().text();
                String hyper = CURRENT_LINK.attr("href");

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
                e.printStackTrace();
            }

        }


        return GglEmd.build();
    }

    @Override
    public String getHelp() {
        return "`" + Callerphone.config.getPrefix() + "search <query>` - Search for something quickly on the web with title, snippet, and link!";
    }

    @Override
    public String[] getTriggers() {
        return "search,ddg,query".split(",");
    }

}
