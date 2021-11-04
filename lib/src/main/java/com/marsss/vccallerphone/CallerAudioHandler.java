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

import java.nio.ByteBuffer;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

public class CallerAudioHandler implements AudioSendHandler, AudioReceiveHandler {

	private int PORT;
	
	public void setPort(int port) {
		PORT = port;
	}
	
	@Override
	public boolean canReceiveCombined() {
		return true;
	}

	@Override
	public void handleCombinedAudio(CombinedAudio combinedAudio) {
		byte[] data = combinedAudio.getAudioData(1.0f);
		AudioStorage.audio[PORT].getCaller().add(data);
	}

	@Override
	public boolean canProvide() {
		return !AudioStorage.audio[PORT].getCaller().isEmpty();
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		byte[] data = AudioStorage.audio[PORT].getReceiver().poll();
		return data == null ? null : ByteBuffer.wrap(data);
	}

	@Override
	public boolean isOpus() {
		return false;
	}
}
