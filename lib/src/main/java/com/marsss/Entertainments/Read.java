package com.marsss.Entertainments;

import java.io.InputStream;
import java.util.ArrayList;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Read {
	static MessageEmbed read(InputStream stream){
		try {
			ArrayList<Character> arr = new ArrayList<>();
			int b;
			while((b = stream.read()) != -1)
				arr.add((char) b);
			char[] unboxed = new char[arr.size()];
			for(int i = 0; i < unboxed.length; i++)
				unboxed[i] = arr.get(i);
			String s = String.valueOf(unboxed);
			EmbedBuilder TextEmd = new EmbedBuilder()
					.setTitle("Received a file! \nBytes read:")
					.setColor(0x7289DA)
					.setDescription(s);

			return TextEmd.build();
		} catch (Exception e) {}
		
		return new EmbedBuilder().setTitle("Received a file! \nUnable to Read Bytes").build();
	}
	
	public static String getHelp() {
		return "`read <textfile>` - Read text files even though Discord has it :D";
	}
}
