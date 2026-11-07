package Discord.playerHandlers;

import Discord.Server;
import com.hawolt.logger.Logger;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>PlayMethods class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class PlayMethods extends AbstractAudioLoadResultHandler {
    public static List<Server> servers = new ArrayList<>();
    protected final TrackScheduler trackScheduler;
    protected final Server server;
    protected final DisconnectTimer dc;

    public PlayMethods(Server server) {
        trackScheduler = server.getTrackScheduler();
        this.server = server;
        this.dc = server.getDc();
    }

    public static String resolveLink(String link) {
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

    @Override
    public void ontrackLoaded(@NonNull TrackLoaded trackLoaded) {

    }

    @Override
    public void onPlaylistLoaded(@NonNull PlaylistLoaded playlistLoaded) {

    }

    @Override
    public void onSearchResultLoaded(@NonNull SearchResult searchResult) {

    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(@NonNull LoadFailed loadFailed) {

    }
}
