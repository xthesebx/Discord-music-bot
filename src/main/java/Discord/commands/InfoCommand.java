package Discord.commands;

import Discord.Server;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * command /info
 * gives info bout current song (name, author, position)
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class InfoCommand extends BasicCommand {
    /**
     * player to not have to retrieve it 10 times
     */
    AudioPlayer player;
    /**
     * <p>Constructor for InfoCommand.</p>
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public InfoCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.player = server.getPlayer();
        if (player.getPlayingTrack() == null) {
            event.reply("Nothing is currently playing").queue();
            return;
        }

        //getting position in song and full duration
        long poshour = (long) Math.floor((double) player.getPlayingTrack().getPosition() / 1000 / 60 / 60);
        long posmin = (long) Math.floor(((double) player.getPlayingTrack().getPosition() / 1000 / 60) % 60);
        long possec = (long) Math.floor(((double) player.getPlayingTrack().getPosition() / 1000) % 60);
        long durhour = (long) Math.floor((double) player.getPlayingTrack().getDuration() / 1000 / 60 / 60);
        long durmin = (long) Math.floor(((double) player.getPlayingTrack().getDuration() / 1000 / 60) % 60);
        long dursec = (long) Math.floor(((double) player.getPlayingTrack().getDuration() / 1000) % 60);
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
        if (durhour > 0)
            time = poshours + ":" + posmins + ":" + possecs + "/" + durhours + ":" + durmins + ":" + dursecs + "```";
        else if (durmin > 0) time = posmins + ":" + possecs + "/" + durmins + ":" + dursecs + "```";
        else time = possecs + "/" + dursecs + "```";
        event.reply("```Currently playing: " + player.getPlayingTrack().getInfo().title +
                " by: " + player.getPlayingTrack().getInfo().author +
                " " + time + " link: " + player.getPlayingTrack().getInfo().uri).queue();
    }
}
