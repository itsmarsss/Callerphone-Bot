package com.marsss.entertainments;

import com.marsss.Bot;

public class Clap {

	public static String clap(String []args) {
		StringBuilder MESSAGE = new StringBuilder("👏");
		for(int i = 1; i < args.length; i++) {
			MESSAGE.append(args[i]).append("👏");
		}
		return MESSAGE.toString();
	}
	
	public static String getHelp() {
		return "`" + Bot.Prefix + "clap` - Add meaning to your messages by having the 👏 emoji between every word.";
	}
}
