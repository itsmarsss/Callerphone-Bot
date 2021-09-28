package com.marsss.entertainments;

public class Clap {

	public static String clap(String []args) {
		StringBuilder MESSAGE = new StringBuilder("ðŸ‘?");
		for(int i = 0; i < args.length; i++) {
			MESSAGE.append(args[i]).append("ðŸ‘?");
		}
		return MESSAGE.toString();
	}
	
	public static String getHelp() {
		return "`clap` - Add meaning to your messages by having the ðŸ‘? emoji between every word!";
	}
}
