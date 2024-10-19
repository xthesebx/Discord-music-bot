package Discord.playerHandlers;

import Discord.Server;
import com.hawolt.logger.Logger;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * Disconnect timer to disconnect after 5 minutes of silent in voice chat
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class DisconnectTimer implements Runnable{
	int i = 0, loops = 300;
	boolean active;
	private final AudioManager audioManager;
	private final Server server;

	/**
	 * constructor of the Disconnect timer, only needs to know audioManager
	 *
	 * @param server Server to get the used information from
	 */
	public DisconnectTimer(Server server) {
		this.audioManager = server.getAudioManager();
		this.server = server;
	}

	/**
	 * {@inheritDoc}
	 *
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
				} else {
					server.leave();
				}
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
