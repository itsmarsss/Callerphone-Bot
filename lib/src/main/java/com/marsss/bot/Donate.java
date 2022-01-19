package com.marsss.bot;

import com.marsss.Bot;

public class Donate {
	public static String donate() {
		return "Donate at <" + Bot.donate + ">";
	}
	
	public static String getHelp() {
		return "`" + Bot.Prefix + "donate` - Help us out by donating.";
	}
}
