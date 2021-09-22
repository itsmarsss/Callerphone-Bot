package com.marsss.Entertainments;

import java.util.Random;

public class RPS {

	static String rps(String []args) {
		Random rand = new Random();
		int c = rand.nextInt(3);
		// 0 = rock
		// 1 = paper
		// 2 = scissors
		if(args[0].toLowerCase().equals("r") && c == 2)
			return "You: Rock\nComputer: Scissors\n**You Win**";

		if(args[0].toLowerCase().equals("r") && c == 1)
			return "You: Rock\nComputer: Paper\n**You Lose**";

		if(args[0].toLowerCase().equals("p") && c == 0)
			return "You: Paper\nComputer: Rock\n**You Win**";

		if(args[0].toLowerCase().equals("p") && c == 2)
			return "You: Paper\nComputer: Scissors\n**You Lose**";

		if(args[0].toLowerCase().equals("s") && c == 1)
			return "You: Scissors\nComputer: Scissors\n**You Win**";

		if(args[0].toLowerCase().equals("s") && c == 0)
			return "You: Scissors\nComputer: Scissors\n**You Lose**";

		if(args[0].toLowerCase().equals("r") && c == 0)
			return "You: Rock\nComputer: Rock\n**You Tied**";
		
		if(args[0].toLowerCase().equals("p") && c == 1)
			return "You: Paper\nComputer: Paper\n**You Tied**";
		
		if(args[0].toLowerCase().equals("s") && c == 2)
			return "You: Scissors\nComputer: Scissors\n**You Tied**";

		return "Invalid move...";
	}
	
	static String getHelp() {
		return "`rps <r/p/s>` - Play rock paper scissors with... a computer 😅";
	}
}
