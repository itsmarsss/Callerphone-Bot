package com.marsss.Entertainments;

import java.util.Random;

public class RPS {

	public static String rps(String move) {
		Random rand = new Random();
		int c = rand.nextInt(3);
		// 0 = rock
		// 1 = paper
		// 2 = scissors
		if(move.toLowerCase().equals("r") && c == 2)
			return "__You:__ Rock\n__Computer:__ Scissors\n**You Win**";

		if(move.toLowerCase().equals("r") && c == 1)
			return "__You:__ Rock\n__Computer:__ Paper\n**You Lose**";

		if(move.toLowerCase().equals("p") && c == 0)
			return "__You:__ Paper\n__Computer:__ Rock\n**You Win**";

		if(move.toLowerCase().equals("p") && c == 2)
			return "__You:__ Paper\n__Computer:__ Scissors\n**You Lose**";

		if(move.toLowerCase().equals("s") && c == 1)
			return "__You:__ Scissors\n__Computer:__ Scissors\n**You Win**";

		if(move.toLowerCase().equals("s") && c == 0)
			return "__You:__ Scissors\n__Computer:__ Scissors\n**You Lose**";

		if(move.toLowerCase().equals("r") && c == 0)
			return "__You:__ Rock\n__Computer:__ Rock\n**You Tied**";
		
		if(move.toLowerCase().equals("p") && c == 1)
			return "__You:__ Paper\n__Computer:__ Paper\n**You Tied**";
		
		if(move.toLowerCase().equals("s") && c == 2)
			return "__You:__ Scissors\n__Computer:__ Scissors\n**You Tied**";

		return "Invalid move...";
	}
	
	static String getHelp() {
		return "`rps <r/p/s>` - Play rock paper scissors with... a computer 😅…";
	}
}
