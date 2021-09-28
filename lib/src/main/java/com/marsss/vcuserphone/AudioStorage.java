package com.marsss.vcuserphone;

import java.util.Queue;

public class AudioStorage {
	public static class Audio {
		public Queue<byte[]> caller;
		public String callerGuildID;
		public String callerChannelID;
		public Queue<byte[]> receiver;
		public String receiverGuildID;
		public String receiverChannelID;
		public boolean connected;

		public Audio(Queue<byte[]> caller, String callerGuildID, String callerChannelID, Queue<byte[]> receiver, String receiverGuildID, String receiverChannelID, boolean connected){
			this.caller = caller;
			this.callerGuildID = callerGuildID;
			this.callerChannelID = callerChannelID;
			this.receiver = receiver;
			this.receiverGuildID = receiverGuildID;
			this.receiverChannelID = receiverChannelID;
			this.connected = connected;
		}
	}
	public static Audio[] audio = new Audio[10];
}
