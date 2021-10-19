package com.marsss.tccallerphone;

import java.util.Queue;

public class ConvoStorage {

	public static class Convo {
		public Queue<String> messages;
		public String callerTCID;

		public String receiverTCID;
		public boolean isConnected;
		
		public long lastMessage;
		
		public boolean report;

		public Convo(Queue<String> messages, String callerTCID, String receiverTCID, boolean isConnected, int lastMessage, boolean report) {
			this.messages = messages;
			this.callerTCID = callerTCID;
			this.receiverTCID = receiverTCID;
			this.isConnected = isConnected;
			this.lastMessage = lastMessage;
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

		public void resetMessage() {
			messages.clear();
			callerTCID = "empty";
			receiverTCID = "";
			isConnected = false;
			lastMessage = 0;
		}
	}
	public static Convo[] convo = new Convo[10000];
}
