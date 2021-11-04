/*
 * Copyright 2021 Marsss (itsmarsss).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marsss.utils;

import java.awt.Color;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.marsss.Bot;
import com.marsss.entertainments.Colour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Search {

	public static MessageEmbed search(String query) throws IOException {

		query = query.substring(1, query.length());
		
		final String url = "https://www.duckduckgo.com/html" + "?q=" + query;	 

		final Document doc = Jsoup.connect(url).get();

		final Elements links = doc.getElementById("links").getElementsByClass("results_links");

		final Color COLOR = Colour.randColor();

		EmbedBuilder GglEmd = new EmbedBuilder()
				.setColor(COLOR)
				.setTitle("Search Results for *" + query + "*", url.replaceAll("\\s+", "%20"));

		for(int i = 0; i < 3; i++) {
			try {
				final Element currlink = links.get(i).getElementsByClass("links_main").first().getElementsByTag("a").first();

				String title = currlink.text();
				String snippet = links.get(i).getElementsByClass("result__snippet").first().text();
				String hyper = currlink.attr("href");

				if(title.length() > 100) {
					title = title.substring(0, 197) + "...";
				}

				if(snippet.length() > 1000) {
					snippet = snippet.substring(0, 1997) + "...";
				}

				GglEmd.addField("__" + title + "__", 
						snippet + 
						"\n[[Link]](" + hyper + ")", false);
			}catch(Exception e) {
				continue;
			}

		}

		
		
		return GglEmd.build();
	}

	public static String getHelp() {
		return "`" + Bot.Prefix + "search <query>` - Search for something quickly on the web with title, snippet, and link!";
	}

}
