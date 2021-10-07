package com.marsss.entertainments;

public class Echo {

	public static String echo(String []args){
		//Nothing to see here
		StringBuilder ECHO = new StringBuilder();
		for(int i = 0; i < args.length; i++) {
			ECHO.append(" ").append(args[i]);
		}
		if(ECHO.toString().isBlank()) {
			return "What do you want me to echo?";
		}
		return ECHO.toString();
	}

	public static String getHelp() {
		return "`u?echo <arg>` - Echo your message for everyone to \"hear\"!";
	}
}
