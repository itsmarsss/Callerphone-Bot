package com.marsss.tccallerphone;

import java.util.Queue;

public class ConvoStorage {

	public static class Convo {
		private Queue<String> messages;
		private String callerTCID;

		private String receiverTCID;
		private boolean isConnected;
		
		private long lastMessage;
		
		private boolean CFF;
		private boolean RFF;
		
		private boolean CAnon;
		private boolean RAnon;
		
		private boolean report;

		public Convo(Queue<String> messages, String callerTCID, String receiverTCID, boolean isConnected, int lastMessage, boolean CFF, boolean RFF, boolean CAnon, boolean RAnon, boolean report) {
			this.messages = messages;
			this.callerTCID = callerTCID;
			this.receiverTCID = receiverTCID;
			this.isConnected = isConnected;
			this.lastMessage = lastMessage;
			this.CFF = CFF;
			this.RFF = RFF;
			this.CAnon = CAnon;
			this.RAnon = RAnon;
			this.report = report;
		}

		// Get

		public Queue<String> getMessages() {
			return messages;
		}

		public String getCallerTCID() {
			return callerTCID;
		}

		public String getReceiverTCID() {
			return receiverTCID;
		}

		public boolean getConnected() {
			return isConnected;
		}
		
		public long getLastMessage() {
			return lastMessage;
		}

		public boolean getCFF() {
			return CFF;
		}
		
		public boolean getRFF() {
			return RFF;
		}
		
		public boolean getCAnon() {
			return CAnon;
		}
		
		public boolean getRAnon() {
			return RAnon;
		}
		
		public boolean getReport() {
			return report;
		}

		// Set
		
		public void addMessage(String message) {
			messages.add(message);
		}

		public void setCallerTCID(String ID) {
			callerTCID = ID;
		}

		public void setReceiverTCID(String ID) {
			receiverTCID = ID;
		}

		public void setConnected(boolean bool) {
			isConnected = bool;
		}
		
		public void setLastMessage(long time) {
			lastMessage = time;
		}

		public void setCFF(boolean ccf) {
			CFF = ccf;
		}
		
		public void setRFF(boolean rff) {
			RFF = rff;
		}
		
		public void setCAnon(boolean canon) {
			CAnon = canon;
		}
		
		public void setRAnon(boolean ranon) {
			RAnon = ranon;
		}
		
		public void setReport(boolean rep) {
			report = rep;
		}

		public void resetMessage() {
			messages.clear();
			callerTCID = "empty";
			receiverTCID = "";
			isConnected = false;
			lastMessage = 0;
			CFF = true;
			CFF = true;
			CAnon = false;
			RAnon = false;
			report = false;
		}
	}
	public static Convo[] convo = new Convo[10000];
}
