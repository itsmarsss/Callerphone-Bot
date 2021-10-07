package com.marsss.entertainments;

public class Clap {

	public static String clap(String []args) {
		StringBuilder MESSAGE = new StringBuilder("👏");
		for(int i = 1; i < args.length; i++) {
			MESSAGE.append(args[i]).append("👏");
		}
		return MESSAGE.toString();
	}
	
	public static String getHelp() {
		return "`u?clap` - Add meaning to your messages by having the 👏 emoji between every word.";
	}
}
