package com.marsss.tccallerphone;

import java.util.Queue;

public class ConvoStorage {

	public static class Convo {
		public Queue<String> messages;
		public String callerTCID;

		public String receiverTCID;
		public boolean isConnected;

		public Convo(Queue<String> messages, String callerTCID, String receiverTCID, boolean isConnected) {
			this.messages = messages;
			this.callerTCID = callerTCID;
			this.receiverTCID = receiverTCID;
			this.isConnected = isConnected;
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
		}
	}
	public static Convo[] convo = new Convo[10000];
}
