package com.marsss.tccallerphone;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.marsss.Bot;
import com.marsss.tccallerphone.ConvoStorage.Convo;

public class Annihilator implements Runnable {

	@Override
	public void run() {
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				kill();
			}
		}, 0, 2, TimeUnit.MINUTES);
	}

	protected void kill() {
		for(Convo c : ConvoStorage.convo) {
			if(!c.isConnected) {
				continue;
			}
			if(System.currentTimeMillis()-c.lastMessage >= 300000) {
				final String callerID = c.getCallerTCID();
				final String receiverID = c.getReceiverTCID();

				String data = "";
				for(String m : c.getMessages())
					data += m + "\n";

				c.resetMessage();
				try {
					Bot.jda.getTextChannelById(callerID).sendMessage(Bot.Callerphone + "Call ended due to inactivity.").queue();
				}catch(Exception ex) {}
				try {
					Bot.jda.getTextChannelById(receiverID).sendMessage(Bot.Callerphone + "Call ended due to inactivity.").queue();
				}catch(Exception ex) {}
				LocalDateTime now = LocalDateTime.now();
				String month = String.valueOf(now.getMonthValue());
				String day = String.valueOf(now.getDayOfMonth());
				String hour = String.valueOf(now.getHour());
				String minute = String.valueOf(now.getMinute());
				String ID = month + day + hour + minute + callerID + receiverID;			

				final String DATA = data;
				if(c.report) {
					Bot.jda.getTextChannelById("897290511000404008").sendMessage("**ID:** " + ID).addFile(DATA.getBytes(), ID + ".txt").queue();
				}
			}
		}
	}

}
