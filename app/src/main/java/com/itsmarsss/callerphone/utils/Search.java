package com.itsmarsss.callerphone.utils;

import com.itsmarsss.commandType.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Search implements ISlashCommand {
    private static final Logger logger = LoggerFactory.getLogger(Search.class);
    private static final int RESULT_COUNT = 3;

    @Override
    public void runSlash(SlashCommandInteractionEvent e) {
        OptionMapping queryOpt = e.getOption("query");
        if (queryOpt == null || queryOpt.getAsString().trim().isEmpty()) {
            e.replyEmbeds(EmbedHelpers.error("Please provide a search query.")).setEphemeral(true).queue();
            return;
        }

        e.deferReply().queue();
        try {
            e.getHook().editOriginalEmbeds(search(queryOpt.getAsString().trim())).queue();
        } catch (Exception ex) {
            logger.error("Search failed for query '{}'", queryOpt.getAsString(), ex);
            e.getHook().editOriginalEmbeds(EmbedHelpers.error("Error fetching search results.")).queue();
        }
    }

    public MessageEmbed search(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        String url = "https://html.duckduckgo.com/html/?q=" + encoded;

        Document doc = Jsoup.connect(url)
                .userAgent("Callerphone-Bot/6.0")
                .timeout(8000)
                .get();

        Element linksRoot = doc.getElementById("links");
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Colour.randColor())
                .setTitle("Search Results for *" + EmbedHelpers.truncate(query, 200) + "*", url);

        if (linksRoot == null) {
            return embed.setDescription("No results found.").build();
        }

        Elements links = linksRoot.getElementsByClass("results_links");
        int added = 0;
        for (int i = 0; i < links.size() && added < RESULT_COUNT; i++) {
            try {
                Element main = links.get(i).getElementsByClass("links_main").first();
                if (main == null) {
                    continue;
                }
                Element anchor = main.getElementsByTag("a").first();
                Element snippetEl = links.get(i).getElementsByClass("result__snippet").first();
                if (anchor == null) {
                    continue;
                }

                String title = EmbedHelpers.truncate(anchor.text(), 100);
                String snippet = snippetEl != null ? EmbedHelpers.truncate(snippetEl.text(), 1000) : "";
                String hyper = anchor.attr("href");

                embed.addField("__" + title + "__", snippet + "\n[[Link]](" + hyper + ")", false);
                added++;
            } catch (Exception ex) {
                logger.debug("Skipping broken search result {}", i, ex);
            }
        }

        if (added == 0) {
            embed.setDescription("No results found.");
        }
        return embed.build();
    }

    @Override
    public String getHelp() {
        return "</search:1075169251431809134> - Search for something quickly on the web with title, snippet, and link!";
    }

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Search the web")
                .addOptions(new OptionData(OptionType.STRING, "query", "Search query").setRequired(true))
                .setContexts(InteractionContextType.GUILD);
    }
}
