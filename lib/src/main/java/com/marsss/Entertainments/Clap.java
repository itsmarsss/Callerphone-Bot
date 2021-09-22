package com.marsss.Entertainments;

public class Clap {

	public static String clap(String []args) {
		StringBuilder message = new StringBuilder("👏");
		for(int i = 1; i < args.length; i++) {
			message.append(args[i]).append("👏");
		}
		return message.toString();
	}
	
	static String getHelp() {
		return "`clap` - Add meaning to your messages by having the 👏 emoji between every word!";
	}
}
