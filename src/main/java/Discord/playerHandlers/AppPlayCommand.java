package Discord.playerHandlers;

import Discord.Server;
import com.hawolt.logger.Logger;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import org.jspecify.annotations.NonNull;


public class AppPlayCommand extends PlayMethods {

    public AppPlayCommand(Server server) {
        super(server);
    }

    @Override
    public void ontrackLoaded(@NonNull TrackLoaded trackLoaded) {
        trackScheduler.queue(trackLoaded.getTrack());
        Logger.error("scheduled");
        servers.remove(server);
        Logger.error("removed");
    }

    @Override
    public void onPlaylistLoaded(@NonNull PlaylistLoaded playlistLoaded) {
        trackScheduler.queue(playlistLoaded.getTracks());
        servers.remove(server);
    }

    @Override
    public void onSearchResultLoaded(@NonNull SearchResult searchResult) {
        trackScheduler.queue(searchResult.getTracks().get(0));
        servers.remove(server);
    }

    @Override
    public void noMatches() {
        Logger.debug("no matches");
        servers.remove(server);
    }

    @Override
    public void loadFailed(@NonNull LoadFailed loadFailed) {
        Logger.debug("load failed");
        servers.remove(server);
    }
}
