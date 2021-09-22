package com.marsss.Entertainments;

public class Echo {

	static String echo(String []args){
		//Nothing to see here
		StringBuilder echo = new StringBuilder();
		for(int i = 1; i < args.length; i++) {
			echo.append(" ").append(args[i]);
		}
		return echo.toString();
	}

	static String getHelp() {
		return "`echo <arg>` - Echos your message for all to \"hear\"!";
	}
}
