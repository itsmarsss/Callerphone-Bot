package com.marsss.callerphone.bot;

import com.marsss.callerphone.Callerphone;

public class Donate {
	public static String donate() {
		return "Donate at <" + Callerphone.donate + ">";
	}
	
	public static String getHelp() {
		return "`" + Callerphone.Prefix + "donate` - Help us out by donating.";
	}
}
