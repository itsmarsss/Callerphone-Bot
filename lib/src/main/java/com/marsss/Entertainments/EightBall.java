package com.marsss.Entertainments;

import java.util.Random;

public class EightBall {
	public static String eightball(String qst){
		String [] answerlist = new String[20];
		answerlist[0] = "It is certain.";
		answerlist[1] = "It is decidedly so.";
		answerlist[2] = "Without a doubt.";
		answerlist[3] = "Yes – definitely.";
		answerlist[4] = "You may rely on it.";
		answerlist[5] = "As I see it, yes.";
		answerlist[6] = "Most likely.";
		answerlist[7] = "Outlook good.";
		answerlist[8] = "Yes.";
		answerlist[9] = "Signs point to yes.";
		answerlist[10] = "Reply hazy, try again.";
		answerlist[11] = "Ask again later.";
		answerlist[12] = "Better not tell you now.";
		answerlist[13] = "Cannot predict now.";
		answerlist[14] = "Concentrate and ask again.";
		answerlist[15] = "Don't count on it.";
		answerlist[16] = "My reply is no.";
		answerlist[17] = "My sources say no.";
		answerlist[18] = "Outlook not so good.";
		answerlist[19] = "Very doubtful.";
		Random rand = new Random();
		int answer = rand.nextInt(answerlist.length);

		return "Answer to `" + qst + "` is... " + answerlist[answer];
	}
	
	public static String getHelp() {
		return "`8ball <question>` - Help you answer hard questions!";
	}
}

