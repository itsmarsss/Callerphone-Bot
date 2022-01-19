package com.marsss.vccallerphone;

import java.util.Queue;

public class AudioStorage {
	public static class Audio {
		private Queue<byte[]> caller;
		private String callerVCID;
		private String callerChannelID;
		private Queue<byte[]> receiver;
		private String receiverVCID;
		private String receiverChannelID;
		private boolean CAnon;
		private boolean RAnon;
		private boolean isConnected;


		public Audio(Queue<byte[]> caller, String callerVCID, String callerChannelID, Queue<byte[]> receiver, String receiverVCID, String receiverChannelID, boolean CAnon, boolean RAnon, boolean isConnected){
			this.caller = caller;
			this.callerVCID = callerVCID;
			this.callerChannelID = callerChannelID;
			this.receiver = receiver;
			this.receiverVCID = receiverVCID;
			this.receiverChannelID = receiverChannelID;
			this.CAnon = CAnon;
			this.RAnon = RAnon;
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
		
		public boolean getCAnon() {
			return CAnon;
		}
		
		public boolean getRAnon() {
			return RAnon;
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
		
		public void setCAnon(boolean canon) {
			CAnon = canon;
		}
		
		public void setRAnon(boolean ranon) {
			RAnon = ranon;
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
			CAnon = false;
			RAnon = false;
			isConnected = false;
		}

	}
	public static Audio[] audio = new Audio[10000];
}
