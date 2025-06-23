package Discord.playerHandlers;

import com.github.topi314.lavasrc.ExtendedAudioPlaylist;
import com.github.topi314.lavasrc.spotify.SpotifyAudioPlaylist;
import com.github.topi314.lavasrc.spotify.SpotifyAudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.IOException;
import java.util.ArrayList;


/*
 * just redone some SpotifySourceManager Functions to allow bigger playlists to be added, up to 5k songs currently
 */

/**
 * <p>SpotifySourceManager class.</p>
 *
 * @see com.github.topi314.lavasrc.spotify.SpotifySourceManager
 * just added songs so bigger playlists are possible
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class SpotifySourceManager extends com.github.topi314.lavasrc.spotify.SpotifySourceManager {

    /**
     * Constructor for Source Manager
     *
     * @param providers no idea, just null
     * @param clientId client id for spotify
     * @param clientSecret secret for spotify
     * @param countryCode country code of your country
     * @param audioPlayerManager audioPlayerManager for the server
     * @param spdc idk, used for lyrics
     */
    public SpotifySourceManager(String[] providers, String clientId, String clientSecret, String countryCode, AudioPlayerManager audioPlayerManager, String spdc) {
        super(clientId, clientSecret, spdc, countryCode, unused -> audioPlayerManager, new DefaultMirroringAudioTrackResolver(providers));
    }

    /** {@inheritDoc} */
    @Override
    public AudioItem getPlaylist(String id, boolean preview) throws IOException {
        var json = this.getJson(API_BASE + "playlists/" + id, false, true);
        if (json == null) {
            return AudioReference.NO_TRACK;
        }
        var tracks = new ArrayList<AudioTrack>();
        JsonBrowser page;
        var offset = 0;
        var pages = 0;
        do {
            page = this.getJson(API_BASE + "playlists/" + id + "/tracks?limit=" + PLAYLIST_MAX_PAGE_ITEMS + "&offset=" + offset, false, true);
            offset += PLAYLIST_MAX_PAGE_ITEMS;

            for (var value : page.get("items").values()) {
                var track = value.get("track");
                if (track.isNull() || track.get("is_local").asBoolean(false) || track.get("type").text().equals("episode")) {
                    continue;
                }
                tracks.add(this.parseTrack(track, preview));
            }

        }
        while (page.get("next").text() != null && ++pages < 50);
        if (tracks.isEmpty()) {
            return AudioReference.NO_TRACK;
        }
        return new SpotifyAudioPlaylist(json.get("name").text(), tracks, ExtendedAudioPlaylist.Type.PLAYLIST, json.get("external_urls").get("spotify").text(), json.get("images").index(0).get("url").text(), json.get("owner").get("display_name").text(), (int) json.get("tracks").get("total").asLong(0));
    }

    /**
     * @see com.github.topi314.lavasrc.spotify.SpotifySourceManager
     */
    private AudioTrack parseTrack(JsonBrowser json, boolean preview) {
        return new SpotifyAudioTrack(
                new AudioTrackInfo(
                        json.get("name").text(),
                        json.get("artists").index(0).get("name").text(),
                        preview ? PREVIEW_LENGTH : json.get("duration_ms").asLong(0),
                        json.get("id").text(),
                        false,
                        json.get("external_urls").get("spotify").text(),
                        json.get("album").get("images").index(0).get("url").text(),
                        json.get("external_ids").get("isrc").text()
                ),
                json.get("album").get("name").text(),
                json.get("album").get("external_urls").get("spotify").text(),
                json.get("artists").index(0).get("external_urls").get("spotify").text(),
                json.get("artists").index(0).get("images").index(0).get("url").text(),
                json.get("preview_url").text(),
                preview,
                this
        );
    }
}
