package Discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * <p>InfoSpammer class.</p>
 *
 * @author sebas
 * @version $Id: $Id
 */
@Deprecated
public class InfoSpammer implements Runnable{
	public boolean isRunning;
	private TextChannel channel;
	private AudioPlayer player;
	private String id;

	/**
	 * <p>init.</p>
	 *
	 * @param channel a {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel} object
	 * @param player a {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer} object
	 */
	public void init(TextChannel channel, AudioPlayer player) {
		this.channel = channel;
		this.player = player;
		String info = getInfo();
		if (info == null) {
			id = channel.sendMessage("Nothing is currently playing").complete().getId();
		} else {
			id = channel.sendMessage("```Currently playing: " + player.getPlayingTrack().getInfo().title +
					" by: " + player.getPlayingTrack().getInfo().author +
					" " + info).complete().getId();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void run () {
		isRunning = true;
		while (isRunning) {
			try {
				Thread.sleep(1000);
				String info = getInfo();
				if (channel.getLatestMessageId().equals(id))
					if (info == null) {
						channel.editMessageById(id, "Nothing is currently playing").complete();
					}
					else
						channel.editMessageById(id, "```Currently playing: " + player.getPlayingTrack().getInfo().title +
						" by: " + player.getPlayingTrack().getInfo().author +
						" " + info).complete();
				else  {
					channel.deleteMessageById(id).complete();
					if (info == null)
						id = channel.sendMessage("Nothing is currently playing").complete().getId();
					else
						id = channel.sendMessage("```Currently playing: " + player.getPlayingTrack().getInfo().title +
							" by: " + player.getPlayingTrack().getInfo().author +
							" " + info).complete().getId();
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	/**
	 * <p>stop.</p>
	 */
	public void stop() {
		if (!isRunning) return;
		isRunning = false;
		channel.deleteMessageById(id).complete();
	}
	
	private String getInfo() {
		if (player.getPlayingTrack() == null) {
			return null;
		}
		
		long poshour = (long) Math.floor((double) player.getPlayingTrack().getPosition() /1000/60/60);
		long posmin = (long) Math.floor(((double) player.getPlayingTrack().getPosition() /1000/60)%60);
		long possec = (long) Math.floor(((double) player.getPlayingTrack().getPosition() /1000)%60);
		long durhour = (long) Math.floor((double) player.getPlayingTrack().getDuration() /1000/60/60);
		long durmin = (long) Math.floor(((double) player.getPlayingTrack().getDuration() /1000/60)%60);
		long dursec = (long) Math.floor(((double) player.getPlayingTrack().getDuration() /1000)%60);
		String poshours = String.valueOf(poshour);
		String posmins = String.valueOf(posmin);
		String possecs = String.valueOf(possec);
		String durhours = String.valueOf(durhour);
		String durmins = String.valueOf(durmin);
		String dursecs = String.valueOf(dursec);
		if (possecs.length() == 1) possecs = "0" + possecs;
		if (dursecs.length() == 1) dursecs = "0" + dursecs;
		if (durmins.length() > posmins.length()) posmins = "0" + posmins;
		if (durhours.length() > poshours.length()) poshours = "0" + poshours;
		String time;
		if (durhour > 0) time = poshours + ":" + posmins + ":" + possecs + "/" + durhours + ":" + durmins + ":" + dursecs + "```";
		else if (durmin > 0) time = posmins + ":" + possecs + "/" + durmins + ":" + dursecs + "```";
		else time = possecs + "/" + dursecs + "```";
		return time;
	}
	
}
