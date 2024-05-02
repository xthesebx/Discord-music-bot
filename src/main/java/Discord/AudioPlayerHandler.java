package Discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * handler for discord audio player which serves it the youtube music in parts (20ms)
 * its basically copy paste so i have not much clue whats happening in here
 */
public class AudioPlayerHandler implements AudioSendHandler {
	private final AudioPlayer audioPlayer;
	private AudioFrame lastFrame;
	
	public AudioPlayerHandler (AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}
	
	@Override
	public boolean canProvide () {
		lastFrame = audioPlayer.provide();
		return lastFrame != null;
	}
	
	@Override
	public ByteBuffer provide20MsAudio () {
		return ByteBuffer.wrap(lastFrame.getData());
	}
	
	@Override
	public boolean isOpus () {
		return true;
	}
}
