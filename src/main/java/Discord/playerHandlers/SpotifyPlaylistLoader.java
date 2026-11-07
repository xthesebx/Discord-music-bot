package Discord.playerHandlers;

import Discord.NewMain;
import Discord.Server;
import com.hawolt.logger.Logger;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.Link;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpotifyPlaylistLoader {

    private static final int BATCH_SIZE = 100; // batch size for Lavalink requests
    private final SpotifyApi spotifyApi;
    private final Server server;

    public SpotifyPlaylistLoader(Server server) throws IOException, ParseException, SpotifyWebApiException {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(NewMain.clientid)
                .setClientSecret(NewMain.clientsecret)
                .build();
        ClientCredentialsRequest request = spotifyApi.clientCredentials().build();
        ClientCredentials credentials = request.execute();

        spotifyApi.setAccessToken(credentials.getAccessToken());
        this.server = server;
    }

    public void loadPlaylist(String playlistId) throws Exception {
        List<String> trackUris = fetchAllTrackUris(playlistId);
        List<List<String>> batches = partition(trackUris, BATCH_SIZE);

        for (List<String> batch : batches) {
            for (String trackUri : batch) {
                Logger.error(trackUri.substring(trackUri.lastIndexOf(":") + 1));
                final Link test = NewMain.client.getOrCreateLink(server.getGuildId());
                test.loadItem("https://open.spotify.com/track/" + trackUri.substring(trackUri.lastIndexOf(":") + 1)).subscribe(new AppPlayCommand(server));
                // optional: small delay to prevent flooding Lavalink
                Thread.sleep(200);
            }
        }
    }

    private List<String> fetchAllTrackUris(String playlistId) throws Exception {
        List<String> uris = new ArrayList<>();
        int offset = 0;
        int limit = 100; // Spotify API maximum per request
        Paging<PlaylistTrack> page;

        do {
            page = spotifyApi.getPlaylistsItems(playlistId)
                    .limit(limit)
                    .offset(offset)
                    .build()
                    .execute();

            for (PlaylistTrack pt : page.getItems()) {
                uris.add(pt.getTrack().getUri());
            }

            offset += page.getItems().length;
        } while (offset < page.getTotal());

        return uris;
    }

    private <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(list.size(), i + batchSize);
            batches.add(list.subList(i, end));
        }
        return batches;
    }
}