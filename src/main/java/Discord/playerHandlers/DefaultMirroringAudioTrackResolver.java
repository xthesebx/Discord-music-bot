package Discord.playerHandlers;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.mirror.MirroringAudioSourceManager;
import com.github.topi314.lavasrc.mirror.MirroringAudioTrack;
import com.github.topi314.lavasrc.mirror.MirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMirroringAudioTrackResolver implements MirroringAudioTrackResolver {

	private static final Logger log = LoggerFactory.getLogger(DefaultMirroringAudioTrackResolver.class);

	private String[] providers = {
		"ytsearch:\"" + MirroringAudioSourceManager.ISRC_PATTERN + "\"",
		"ytsearch:" + MirroringAudioSourceManager.QUERY_PATTERN
	};

	public DefaultMirroringAudioTrackResolver(String[] providers) {
		if (providers != null && providers.length > 0) {
			this.providers = providers;
		}
	}

	@Override
	public AudioItem apply(MirroringAudioTrack mirroringAudioTrack) {
		AudioItem track = AudioReference.NO_TRACK;
		for (var provider : providers) {
			if (provider.startsWith(SpotifySourceManager.SEARCH_PREFIX)) {
				log.warn("Can not use spotify search as search provider!");
				continue;
			}

			if (provider.startsWith(AppleMusicSourceManager.SEARCH_PREFIX)) {
				log.warn("Can not use apple music search as search provider!");
				continue;
			}

			if (provider.contains(MirroringAudioSourceManager.ISRC_PATTERN)) {
				if (mirroringAudioTrack.getInfo().isrc != null && !mirroringAudioTrack.getInfo().isrc.isEmpty()) {
					provider = provider.replace(MirroringAudioSourceManager.ISRC_PATTERN, mirroringAudioTrack.getInfo().isrc);
				} else {
					log.debug("Ignoring identifier \"{}\" because this track does not have an ISRC!", provider);
					continue;
				}
			}

			provider = provider.replace(MirroringAudioSourceManager.QUERY_PATTERN, getTrackTitle(mirroringAudioTrack));
			com.hawolt.logger.Logger.debug(provider);
			try {
				track = mirroringAudioTrack.loadItem(provider);
			}
			catch (Exception e) {
				log.error("Failed to load track from provider \"{}\"!", provider, e);
			}
			if (track != AudioReference.NO_TRACK) {
				break;
			}
		}

		return track;
	}

	public String getTrackTitle(MirroringAudioTrack mirroringAudioTrack) {
		var query = mirroringAudioTrack.getInfo().title;
		if (!mirroringAudioTrack.getInfo().author.equals("unknown")) {
			query += " " + mirroringAudioTrack.getInfo().author;
		}
		return query;
	}

}
