package Discord;

import net.dv8tion.jda.api.managers.AudioManager;

public class DisconnectTimer implements Runnable{
	int i = 0, loops = 300;
	boolean active = true;
	AudioManager audioManager;
	public DisconnectTimer(AudioManager audioManager) {
		this.audioManager = audioManager;
	}

	@Override
	public void run () {
		timer();
	}

	private void timer() {
		while (true) {
			if (active) {
				if (i < loops) {
					try {
						Thread.sleep(1000);

					} catch (InterruptedException e) {

					}
				} else audioManager.closeAudioConnection();
			}
		}
	}

	public void stopTimer() {
		active = false;
	}

	public void startTimer() {
		active = true;
		i = 0;
	}
}
