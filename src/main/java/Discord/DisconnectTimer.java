package Discord;

import com.hawolt.logger.Logger;
import net.dv8tion.jda.api.managers.AudioManager;

public class DisconnectTimer implements Runnable{
	int i = 0, loops = 300;
	boolean active;
	AudioManager audioManager;
	public DisconnectTimer(AudioManager audioManager) {
		this.audioManager = audioManager;
	}

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

	public void stopTimer() {
		this.active = false;
	}

	public void startTimer() {
		this.active = true;
		i = 0;
	}
}
