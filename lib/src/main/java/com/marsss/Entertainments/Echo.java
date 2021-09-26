package com.marsss.Entertainments;

public class Echo {

	public static String echo(String []args){
		//Nothing to see here
		StringBuilder ECHO = new StringBuilder();
		for(int i = 0; i < args.length; i++) {
			ECHO.append(" ").append(args[i]);
		}
		return ECHO.toString();
	}

	public static String getHelp() {
		return "`echo <arg>` - Echos your message for all to \"hear\"!";
	}
}