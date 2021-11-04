/*
 * Copyright 2021 Marsss (itsmarsss).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		public boolean CAnon;
		public boolean RAnon;
		public boolean isConnected;


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
			CAnon = false;
			RAnon = false;
			isConnected = false;
		}

	}
	public static Audio[] audio = new Audio[10000];
}
