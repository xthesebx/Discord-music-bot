package Discord.playerHandlers;

import Discord.Server;
import dev.arbjerg.lavalink.client.player.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class TwitchPlayCommand extends PlayMethods {

    public TwitchPlayCommand(Server server) {
        super(server);
    }

    @Override
    public void ontrackLoaded (@NonNull TrackLoaded audioTrack) {
        trackScheduler.request(audioTrack.getTrack());
        server.getChatBotListener().addedRequest(audioTrack.getTrack());
        dc.stopTimer();
        servers.remove(server);
    }

    @Override
    public void onPlaylistLoaded (@NonNull PlaylistLoaded audioPlaylist) {
        server.getChatBotListener().print("No Playlists allowed, only single songs");
        servers.remove(server);
    }

    @Override
    public void noMatches() {
        server.getChatBotListener().print("No Song with that url found");
        servers.remove(server);
    }

    @Override
    public void loadFailed (@NotNull LoadFailed e) {
        server.getChatBotListener().print("Error I guess");
        servers.remove(server);
    }


    @Override
    public void onSearchResultLoaded(@NonNull SearchResult searchResult) {
        servers.remove(server);
    }
}
