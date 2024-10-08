package Discord;

import Discord.commands.*;
import com.github.topi314.lavalyrics.LyricsManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.*;
import java.util.*;

/**
 * The class for every server
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class Server {

    /**
     * setter for Volume
     *
     * @param volume volume to set
     */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * getter for guildId
     *
     * @return guildId as string
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * getter for AudioManager
     *
     * @return the AudioManager
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * getter for TrackScheduler
     *
     * @return the TrackScheduler
     */
    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    /**
     * getter for AudioPlayer
     *
     * @return the AudioPlayer
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * getter for Dc
     *
     * @return the Dc
     */
    public DisconnectTimer getDc() {
        return dc;
    }

    /**
     * getter for AudioPlayerManager
     *
     * @return the AudioPlayerManager
     * currently not in use
     */
    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    /**
     * getter for LyricsManager
     *
     * @return the LyricsManager
     */
    public LyricsManager getLyricsManager() {
        return lyricsManager;
    }

    private final String guildId;
    private int volume;

    public Guild getGuild() {
        return guild;
    }

    public Member getStreamer() {
        return streamer;
    }

    public void setStreamer(Member streamer) {
        this.streamer = streamer;
    }

    private Member streamer;
    private final Guild guild;
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final AudioPlayer player = audioPlayerManager.createPlayer();
    private final AudioManager audioManager;
    private final TrackScheduler trackScheduler;
    private final AudioPlayerHandler audioPlayerHandler = new AudioPlayerHandler(player);
    private final DisconnectTimer dc;
    private final AudioTrack[] tracks = new AudioTrack[5];
    private final LyricsManager lyricsManager = new LyricsManager();


    public ChatBotListener getChatBotListener() {
        return chatBotListener;
    }

    private final ChatBotListener chatBotListener = new ChatBotListener(this);

    /**
     * Server creation
     *
     * @param guild to get needed info from
     */
    public Server(Guild guild) throws IOException {
        this.guild = guild;
        guildId = guild.getId();
        volume = readVolume();
        YoutubeAudioSourceManager ytsrc = new YoutubeAudioSourceManager(true, new WebWithThumbnail(), new AndroidMusicWithThumbnail(), new TvHtml5EmbeddedWithThumbnail(), new MusicWithThumbnail());
        audioPlayerManager.registerSourceManager(ytsrc);
        //???? other clients work, i guess i just do this for now
        SpotifySourceManager spsrc = new SpotifySourceManager(null, NewMain.clientid, NewMain.clientsecret, "de", audioPlayerManager, NewMain.spdc);
        audioPlayerManager.registerSourceManager(spsrc);
        lyricsManager.registerLyricsManager(spsrc);
        /*
         * okay wtf i gotta rant:
         * WHY TF IS THIS DEPENDANT ON THE YOUTUBE SOURCE MANAGER?!?!?
         * FIXING THE FIRST ONE FIXED SPOTIFY FOR NO REASON
         * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
         * I DONT GET IT; IT MAKES 0 SENSE
         */

        this.audioManager = guild.getAudioManager();
        trackScheduler = new TrackScheduler(this);
        player.addListener(trackScheduler);
        dc = new DisconnectTimer(this);
        Thread dcThread = new Thread(dc);
        dcThread.start();
        player.setVolume(volume);
    }

    /**
     * read volume file and create if not exists to set volume
     * @return the volume int
     */
    private int readVolume() {
        File f = new File("volumes/" + guildId);
        f.getParentFile().mkdirs();
        if (!f.exists()) {
            NewMain.write("100", f);
        }
        return Integer.parseInt(NewMain.read(f));
    }

    /**
     * join function to join channels
     *
     * @param event event of the channel to join
     * @return the JoinState so we know what happened/how it went
     */
    public JoinStates join (SlashCommandInteractionEvent event) {
        //TODO: Monitor, might have some issues doing it regularly or something, sometimes get rate limits out of nowhere
        dc.startTimer();
        if (event.getMember().getVoiceState().getChannel() == null) {
            return JoinStates.NOTINVOICE;
        }
        if (!guild.getSelfMember().hasPermission(event.getMember().getVoiceState().getChannel(), Permission.VOICE_CONNECT)) {
            // The bot does not have permission to join any voice channel. Don't forget the .queue()!
            return JoinStates.NOPERMS;
        }
        // Creates a variable equal to the channel that the user is in.

        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
        // Checks if they are in a channel -- not being in a channel means that the variable = null.
        // Gets the audio manager.
        if (audioManager.isConnected() && audioManager.getConnectedChannel().asVoiceChannel().equals(connectedChannel)) {
            return JoinStates.ALREADYCONNECTED;
        }
        audioManager.setSendingHandler(audioPlayerHandler);
        // Connects to the channel.
        audioManager.openAudioConnection(connectedChannel);
        // Obviously people do not notice someone/something connecting.
        return JoinStates.JOINED;
    }

    /**
     * creating the Command responses for the SlashCommandInteractionEvents
     *
     * @param event the event coming from the newMain
     */
    public void onSlashCommandInteraction (SlashCommandInteractionEvent event) {
        String s = event.getName();
        if (streamer != null && (!event.getMember().equals(streamer) || !event.getUser().getId().equals("277064996264083456"))) {
            event.reply("streamer mode is active!").queue();
            return;
        }
        switch (s) {
            case "help" -> new HelpCommand(event, this);
            case "info" -> new InfoCommand(event, this);
            case "join" -> new JoinCommand(event, this);
            case "leave" -> new LeaveCommand(event, this);
            case "pause" -> new PauseCommand(event, this);
            case "play" -> new PlayCommand(event, this);
            case "queue" -> new QueueCommand(event, this);
            case "resume" -> new ResumeCommand(event, this);
            case "stop" -> new StopCommand(event, this);
            case "volume" -> new VolumeCommand(event, this);
            case "skip" -> new SkipCommand(event, this);
            case "shuffle" -> new ShuffleCommand(event, this);
            case "repeat" -> new RepeatCommand(event, this);
            case "lyrics" -> new LyricsCommand(event, this);
            case "streamermode" -> new StreamerModeCommands(event, this);
            case "streamerrole" -> new StreamerRoleCommand(event, this);
            case "hotkeyconnect" -> new HotkeyConnectionCommand(event, this);
        }
    }

    /**
     * creating the Command responses for the ButtonInteractionEvents
     *
     * @param event the event coming from the newMain
     */
    public void onButtonInteraction (ButtonInteractionEvent event) {
        if (streamer != null && (!event.getMember().equals(streamer) || !event.getUser().getId().equals("277064996264083456"))) {
            event.reply("streamer mode is active!").queue();
            return;
        }
        event.getMessage().delete().queue();
        event.deferReply().queue();
        try {
            AudioTrack track = tracks[Integer.parseInt(event.getButton().getId())];
            if (track != null) {
                trackScheduler.queue(track);
                event.getHook().editOriginal("```Added " + track.getInfo().title + " by " + track.getInfo().author + " to queue```").queue();
            } else event.getHook().editOriginal("```Search is no longer available due to a bot restart```").queue();
        } catch (NullPointerException e) {
            event.getHook().editOriginal("```Button is from old Bot Task, cant execute it```").queue();
        }
    }

    /**
     * to play the actual thingy
     *
     * @param link link or songtitle to search for/play
     * @param event is the event, either buttoninteraction or slashcommandinteraction
     */
    public void play(String link, SlashCommandInteractionEvent event) {
        play(link, event, 0);
    }

    /**
     * to play the actual thingy
     *
     * @param link link or songtitle to search for/play
     * @param event is the event, either buttoninteraction or slashcommandinteraction
     * @param retries number of iterations
     */
    public void play(String link, SlashCommandInteractionEvent event, int retries) {
        audioPlayerManager.loadItem(link, new AudioLoadResultHandler() {
            String text;

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                trackScheduler.queue(audioTrack);
                text = "```Added \"" + audioTrack.getInfo().title + "\" by " + audioTrack.getInfo().author + " to Queue```";
                event.getHook().editOriginal(text).queue();
            }

            @Override
            public void playlistLoaded (AudioPlaylist audioPlaylist) {
                if (link.startsWith("ytsearch:") || link.startsWith("ytmsearch:") || link.startsWith("spsearch:")) {
                    dc.stopTimer();
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
                if (retries < 5) play(link, event, retries+1);
                else event.getHook().editOriginal(e.getMessage()).queue();
            }
        });
    }

    /**
     * for usage in streamer mode
     * @param link link to song request
     */

    public void play(String link) {
        audioPlayerManager.loadItem(link, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                trackScheduler.request(audioTrack);
            }

            @Override
            public void playlistLoaded (AudioPlaylist audioPlaylist) {
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed (FriendlyException e) {
            }
        });
    }
}
