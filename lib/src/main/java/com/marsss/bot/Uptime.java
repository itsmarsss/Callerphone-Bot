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
package com.marsss.bot;

import java.lang.management.ManagementFactory;

import com.marsss.Bot;

public class Uptime {

	public static String uptime() {

		// https://github.com/DV8FromTheWorld/Yui/blob/master/src/main/java/net/dv8tion/discord/commands/UptimeCommand.java {

		final long DURATION = ManagementFactory.getRuntimeMXBean().getUptime();

		final long YEARS = DURATION / 31104000000L;
		final long MONTHS = DURATION / 2592000000L % 12;
		final long DAYS = DURATION / 86400000L % 30;
		final long HOURS = DURATION / 3600000L % 24;
		final long MINUTES = DURATION / 60000L % 60;
		final long SECONDS = DURATION / 1000L % 60;
		final long MILLISECONDS = DURATION % 1000;

		String UPTIME = (YEARS == 0 ? "" : "**" + YEARS + "** years, ") + 
				(MONTHS == 0 ? "" : "**" + MONTHS + "** months, ") + 
				(DAYS == 0 ? "" : "**" + DAYS + "** days, ") + 
				(HOURS == 0 ? "" : "**" + HOURS + "** hours, ") + 
				(MINUTES == 0 ? "" : "**" + MINUTES + "** minutes, ") + 
				(SECONDS == 0 ? "" : "**" + SECONDS + "** seconds, ") + 
				(MILLISECONDS == 0 ? "" : "**" + MILLISECONDS + "** milliseconds, ");

		UPTIME = replaceLast(UPTIME, ", ", "");
		UPTIME = replaceLast(UPTIME, ",", " and");

		return "I've been online for " + UPTIME;
	}
	
	public static String uptimeabt() {

		// https://github.com/DV8FromTheWorld/Yui/blob/master/src/main/java/net/dv8tion/discord/commands/UptimeCommand.java {

		final long DURATION = ManagementFactory.getRuntimeMXBean().getUptime();

		final long YEARS = DURATION / 31104000000L;
		final long MONTHS = DURATION / 2592000000L % 12;
		final long DAYS = DURATION / 86400000L % 30;
		final long HOURS = DURATION / 3600000L % 24;
		final long MINUTES = DURATION / 60000L % 60;
		final long SECONDS = DURATION / 1000L % 60;

		String UPTIME = (YEARS == 0 ? "" : YEARS + "y ") + 
				(MONTHS == 0 ? "" : MONTHS + "M ") + 
				(DAYS == 0 ? "" : DAYS + "d ") + 
				(HOURS == 0 ? "" : HOURS + "h ") + 
				(MINUTES == 0 ? "" : MINUTES + "m ") + 
				(SECONDS == 0 ? "" : SECONDS + "s ");

		UPTIME = replaceLast(UPTIME, ", ", "");
		UPTIME = replaceLast(UPTIME, ",", " and");

		return UPTIME;
	}

	private static String replaceLast(final String text, final String regex, final String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

	// }

	public static String getHelp() {
		return "`" + Bot.Prefix + "uptime` - Gets the bot's uptime.";
	}
}
