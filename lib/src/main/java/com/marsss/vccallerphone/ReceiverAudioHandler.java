package com.marsss.vccallerphone;

import java.nio.ByteBuffer;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

public class ReceiverAudioHandler implements AudioSendHandler, AudioReceiveHandler {

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
		AudioStorage.audio[PORT].getReceiver().add(data);
	}

	@Override
	public boolean canProvide() {
		return !AudioStorage.audio[PORT].getReceiver().isEmpty();
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		byte[] data = AudioStorage.audio[PORT].getCaller().poll();
		return data == null ? null : ByteBuffer.wrap(data);
	}

	@Override
	public boolean isOpus() {
		return false;
	}
}
