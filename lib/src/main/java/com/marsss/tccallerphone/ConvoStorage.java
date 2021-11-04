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
package com.marsss.tccallerphone;

import java.util.Queue;

public class ConvoStorage {

	public static class Convo {
		public Queue<String> messages;
		public String callerTCID;

		public String receiverTCID;
		public boolean isConnected;
		
		public long lastMessage;
		
		public boolean CFF;
		public boolean RFF;
		
		public boolean CAnon;
		public boolean RAnon;
		
		public boolean report;

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
			CFF = true;
			CFF = true;
			CAnon = false;
			RAnon = false;
			report = false;
		}
	}
	public static Convo[] convo = new Convo[10000];
}
