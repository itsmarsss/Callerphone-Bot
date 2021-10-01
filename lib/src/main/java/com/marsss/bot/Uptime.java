package com.marsss.bot;

import java.lang.management.ManagementFactory;

public class Uptime {

	public static String uptime() {

		final long DURATION = ManagementFactory.getRuntimeMXBean().getUptime();

		final long YEARS = DURATION / 31104000000L;
		final long MONTHS = DURATION / 2592000000L % 12;
		final long DAYS = DURATION / 86400000L % 30;
		final long HOURS = DURATION / 3600000L % 24;
		final long MINUTES = DURATION / 60000L % 60;
		final long SECONDS = DURATION / 1000L % 60;
		final long MILLISECONDS = DURATION % 1000;

		String UPTIME = (YEARS == 0 ? "" : "**" + YEARS + "** Years, ") + 
				(MONTHS == 0 ? "" : "**" + MONTHS + "** Months, ") + 
				(DAYS == 0 ? "" : "**" + DAYS + "** Days, ") + 
				(HOURS == 0 ? "" : "**" + HOURS + "** Hours, ") + 
				(MINUTES == 0 ? "" : "**" + MINUTES + "** Minutes, ") + 
				(SECONDS == 0 ? "" : "**" + SECONDS + "** Seconds, ") + 
				(MILLISECONDS == 0 ? "" : "**" + MILLISECONDS + "** Milliseconds, ");

		UPTIME = replaceLast(UPTIME, ", ", "");
		UPTIME = replaceLast(UPTIME, ",", " and");

		return "I've been online for " + UPTIME;
	}
	
	private static String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

	public static String getHelp() {
		return "`uptime` - Gets the bot's uptime.";
	}
	
}
