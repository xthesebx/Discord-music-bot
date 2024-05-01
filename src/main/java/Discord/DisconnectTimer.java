package Discord;

import net.dv8tion.jda.api.managers.AudioManager;

public class DisconnectTimer implements Runnable{
	
	AudioManager audioManager;
	public DisconnectTimer(AudioManager audioManager) {
		this.audioManager = audioManager;
	}
	@Override
	public void run () {
		try {
			Thread.sleep(300000);
			audioManager.closeAudioConnection();
		} catch (InterruptedException ignored) {
		
		}
	}
}
