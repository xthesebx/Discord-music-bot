package Discord.playerHandlers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * handler for discord audio player which serves it the youtube music in parts (20ms)
 * its basically copy paste so i have not much clue whats happening in here
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class AudioPlayerHandler implements AudioSendHandler {
	private final AudioPlayer audioPlayer;
	private AudioFrame lastFrame;
	
	/**
	 * <p>Constructor for AudioPlayerHandler.</p>
	 *
	 * @param audioPlayer a {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer} object
	 */
	public AudioPlayerHandler (AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean canProvide () {
		lastFrame = audioPlayer.provide();
		return lastFrame != null;
	}
	
	/** {@inheritDoc} */
	@Override
	public ByteBuffer provide20MsAudio () {
		return ByteBuffer.wrap(lastFrame.getData());
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isOpus () {
		return true;
	}
}
