package Discord;

import com.hawolt.logger.Logger;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * Disconnect timer to disconnect after 5 minutes of silent in voice chat
 */
public class DisconnectTimer implements Runnable{
	int i = 0, loops = 300;
	boolean active;
	AudioManager audioManager;
	public DisconnectTimer(AudioManager audioManager) {
		this.audioManager = audioManager;
	}

	/**
	 * just loop this shit
	 */
	@Override
	public void run () {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Logger.error(e);
			}
			if (this.active) {
				if (i < loops) {
					i++;
				} else audioManager.closeAudioConnection();
			}
		}
	}

	/**
	 * starts timer
	 */
	public void stopTimer() {
		this.active = false;
	}

	/**
	 * stops timer
	 */
	public void startTimer() {
		this.active = true;
		i = 0;
	}
}
