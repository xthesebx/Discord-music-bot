package Discord.playerHandlers;

import Discord.Server;
import com.hawolt.logger.Logger;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * <p>PlayMethods class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class PlayMethods {

    /**
     * to play the actual thingy
     *
     * @param link link or songtitle to search for/play
     * @param event is the event, either buttoninteraction or slashcommandinteraction
     * @param server a {@link Discord.Server} object
     */
    public static void play(String link, SlashCommandInteractionEvent event, Server server) {
        play(link, event, 0, server);
    }

    /**
     * to play the actual thingy
     *
     * @param link link or songtitle to search for/play
     * @param event is the event, either buttoninteraction or slashcommandinteraction
     * @param retries number of iterations
     * @param server a {@link Discord.Server} object
     */
    public static void play(String link, SlashCommandInteractionEvent event, int retries, Server server) {
        link = resolveLink(link);
        AudioPlayerManager audioPlayerManager = server.getAudioPlayerManager();
        TrackScheduler trackScheduler = server.getTrackScheduler();
        DisconnectTimer dc = server.getDc();
        AudioTrack[] tracks = server.getTracks();


        String finalLink = link;
        audioPlayerManager.loadItem(link, new AudioLoadResultHandler() {
            String text;

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                dc.stopTimer();
                trackScheduler.queue(audioTrack);
                text = "```Added \"" + audioTrack.getInfo().title + "\" by " + audioTrack.getInfo().author + " to Queue```";
                event.getHook().editOriginal(text).queue();
            }

            @Override
            public void playlistLoaded (AudioPlaylist audioPlaylist) {
                dc.stopTimer();
                if (finalLink.startsWith("ytsearch:") || finalLink.startsWith("ytmsearch:") || finalLink.startsWith("spsearch:")) {
                    int x = 5;
                    if (audioPlaylist.getTracks().size() < x) x = audioPlaylist.getTracks().size();
                    Button[] rows = new Button[x];
                    List<AudioTrack> list = audioPlaylist.getTracks();
                    for (int i = 0; i < x; i++) {
                        AudioTrack track = list.get(i);
                        tracks[i] = track;
                        String title = track.getInfo().title;
                        String author = track.getInfo().author;
                        if ((title.length() + author.length()) > 76)
                            rows[i] = Button.primary(String.valueOf(i), title.substring(0, 75 - author.length()) + " by " + author);
                        else
                            rows[i] = Button.primary(String.valueOf(i), track.getInfo().title + " by " + track.getInfo().author);
                    }
                    MessageEditData messageEditData = new MessageEditBuilder().setActionRow(rows).setContent("Which one?").build();
                    event.getHook().editOriginal(messageEditData).queue();
                    return;
                }

                for (AudioTrack track : audioPlaylist.getTracks()) {
                    trackScheduler.queue(track);
                }
                text = "```Added \"" + audioPlaylist.getName() + "\" to Queue```";
                event.getHook().editOriginal(text).queue();
            }

            @Override
            public void noMatches() {
                event.getHook().editOriginal("Could not find a song under that link or with that name. To search YouTube use \"ytsearch:\" as prefix for normal videos " +
                        "and \"ytmsearch:\" for YouTube Music search. You can also search on Spotify with \"spsearch:\".").queue();
            }

            @Override
            public void loadFailed (FriendlyException e) {
                if (retries < 5) play(finalLink, event, retries+1, server);
                else event.getHook().editOriginal(e.getMessage()).queue();
            }
        });
    }

    /**
     * for usage in streamer mode
     *
     * @param link link to song request
     * @param server a {@link Discord.Server} object
     */
    public static void play(String link, Server server) {
        link = resolveLink(link);
        AudioPlayerManager audioPlayerManager = server.getAudioPlayerManager();
        TrackScheduler trackScheduler = server.getTrackScheduler();
        DisconnectTimer dc = server.getDc();
        audioPlayerManager.loadItem(link, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                trackScheduler.request(audioTrack);
                server.getChatBotListener().addedRequest(audioTrack);
                dc.stopTimer();
            }

            @Override
            public void playlistLoaded (AudioPlaylist audioPlaylist) {
                server.getChatBotListener().print("No Playlists allowed, only single songs");
            }

            @Override
            public void noMatches() {
                server.getChatBotListener().print("No Song with that url found");
            }

            @Override
            public void loadFailed (FriendlyException e) {
                server.getChatBotListener().print("Error I guess");
            }
        });
    }

    /**
     * <p>playApp.</p>
     *
     * @param link a {@link java.lang.String} object
     * @param server a {@link Discord.Server} object
     */
    public static void playApp(String link, Server server) {
        if (!link.startsWith("http")) {
            link = "ytsearch:" + link;
        }
        link = resolveLink(link);
        AudioPlayerManager audioPlayerManager = server.getAudioPlayerManager();
        TrackScheduler trackScheduler = server.getTrackScheduler();
        DisconnectTimer dc = server.getDc();

        String finalLink = link;
        audioPlayerManager.loadItem(link, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                trackScheduler.queue(audioTrack);
                dc.stopTimer();
            }

            @Override
            public void playlistLoaded (AudioPlaylist audioPlaylist) {
                if (finalLink.startsWith("ytsearch")) {
                    trackScheduler.queue(audioPlaylist.getTracks().get(0));
                    dc.stopTimer();
                    return;
                }
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    trackScheduler.queue(track);
                }
                dc.stopTimer();
            }

            @Override
            public void noMatches() {
                Logger.debug("no matches");
            }

            @Override
            public void loadFailed (FriendlyException e) {
                Logger.debug("load failed");
            }
        });
    }

    private static String resolveLink(String link) {
        if (link.startsWith("http") && (!link.contains("spotify") && !link.contains("youtu"))) {
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                link = connection.getHeaderField("Location");
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        return link;
    }
}
