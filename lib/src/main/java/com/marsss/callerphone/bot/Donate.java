package com.marsss.callerphone.bot;

import com.marsss.callerphone.Bot;

public class Donate {
	public static String donate() {
		return "Donate at <" + Bot.donate + ">";
	}
	
	public static String getHelp() {
		return "`" + Bot.Prefix + "donate` - Help us out by donating.";
	}
}
