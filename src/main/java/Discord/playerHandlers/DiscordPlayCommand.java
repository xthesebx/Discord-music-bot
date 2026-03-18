package Discord.playerHandlers;

import Discord.Server;
import com.hawolt.logger.Logger;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscordPlayCommand extends PlayMethods {

    String text = "";
    private final SlashCommandInteractionEvent event;

    public DiscordPlayCommand(SlashCommandInteractionEvent event, Server server) {
        super(server);
        this.event = event;
    }

    @Override
    public void ontrackLoaded(@NonNull TrackLoaded trackLoaded) {
        trackScheduler.queue(trackLoaded.getTrack());
        text = "```Added \"" + trackLoaded.getTrack().getInfo().getTitle() + "\" by " + trackLoaded.getTrack().getInfo().getAuthor() + " to Queue```";
        event.getHook().editOriginal(text).queue();
        servers.remove(server);
    }

    @Override
    public void onPlaylistLoaded(@NonNull PlaylistLoaded playlistLoaded) {
        dc.stopTimer();
        /*if (finalLink.startsWith("ytsearch:") || finalLink.startsWith("ytmsearch:") || finalLink.startsWith("spsearch:")) {
            int x = 5;
            if (audioPlaylist.getTracks().size() < x) x = audioPlaylist.getTracks().size();
            Button[] rows = new Button[x];
            final List<Track> list = new ArrayList<>();
            audioPlaylist.getTracks().forEach(track -> list.add(new Track(track)));
            for (int i = 0; i < x; i++) {
                Track track = list.get(i);
                tracks[i] = track;
                String title = track.getInfo().title;
                String author = track.getInfo().author;
                if ((title.length() + author.length()) > 76)
                    rows[i] = Button.primary(String.valueOf(i), title.substring(0, 75 - author.length()) + " by " + author);
                else
                    rows[i] = Button.primary(String.valueOf(i), track.getInfo().title + " by " + track.getInfo().author);
            }
            MessageEditData messageEditData = new MessageEditBuilder().setComponents(ActionRow.of(Arrays.asList(rows))).setContent("Which one?").build();
            event.getHook().editOriginal(messageEditData).queue();
            return;
        }*/
        OptionMapping optionMapping = event.getOption("name");
        assert optionMapping != null;
        String link = optionMapping.getAsString();
        Logger.error("playlist: {}", link);
        trackScheduler.queue(playlistLoaded.getTracks());
        text = "```Added \"" + playlistLoaded.getInfo().getName() + "\" to Queue```";
        event.getHook().editOriginal(text).queue();
        servers.remove(server);
    }

    @Override
    public void onSearchResultLoaded(@NonNull SearchResult searchResult) {
        OptionMapping optionMapping = event.getOption("name");
        assert optionMapping != null;
        String link = optionMapping.getAsString();
        Logger.error("search: {}", link);
        servers.remove(server);
    }

    @Override
    public void noMatches() {
        event.getHook().editOriginal("Could not find a song under that link or with that name. To search YouTube use \"ytsearch:\" as prefix for normal videos " +
                "and \"ytmsearch:\" for YouTube Music search. You can also search on Spotify with \"spsearch:\".").queue();

        servers.remove(server);
    }

    @Override
    public void loadFailed(@NonNull LoadFailed loadFailed) {
       event.getHook().editOriginal(loadFailed.getException().getMessage()).queue();
       Logger.error(loadFailed.getException());
       servers.remove(server);
    }
}
