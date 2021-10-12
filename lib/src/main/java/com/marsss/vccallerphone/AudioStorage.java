package com.marsss.vccallerphone;

import java.util.Queue;

public class AudioStorage {
	public static class Audio {
		public Queue<byte[]> caller;
		public String callerVCID;
		public String callerChannelID;
		public Queue<byte[]> receiver;
		public String receiverVCID;
		public String receiverChannelID;
		public boolean isConnected;

		public Audio(Queue<byte[]> caller, String callerVCID, String callerChannelID, Queue<byte[]> receiver, String receiverVCID, String receiverChannelID, boolean isConnected){
			this.caller = caller;
			this.callerVCID = callerVCID;
			this.callerChannelID = callerChannelID;
			this.receiver = receiver;
			this.receiverVCID = receiverVCID;
			this.receiverChannelID = receiverChannelID;
			this.isConnected = isConnected;
		}
		
		// Get
		
		public Queue<byte[]> getCaller() {
			return caller;
		}
		
		public String getCallerVCID() {
			return callerVCID;	
		}
		
		public String getCallerChannelID() {
			return callerChannelID;	
		}
		
		public Queue<byte[]> getReceiver() {
			return receiver;
		}
		
		public String getReceiverVCID() {
			return receiverVCID;	
		}
		
		public String getReceiverChannelID() {
			return receiverChannelID;	
		}
		
		public boolean getConnected() {
			return isConnected;	
		}
		
		
		
		// Set
		
		public void setCallerVCID(String ID) {
			callerVCID = ID;
		}
		
		public void setCallerChannelID(String ID) {
			callerChannelID = ID;
		}
		
		public void setReceiverVCID(String ID) {
			receiverVCID = ID;
		}
		
		public void setReceiverChannelID(String ID) {
			receiverChannelID = ID;
		}
		
		public void setConnected(boolean bool) {
			isConnected = bool;
		}
		
		public void resetAudio() {
			caller.clear();
			callerVCID = "empty";
			callerChannelID = "";
			receiver.clear();
			receiverVCID = "";
			receiverChannelID = "";
			isConnected = false;
		}

	}
	public static Audio[] audio = new Audio[10000];
}
