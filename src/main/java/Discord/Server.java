package Discord;

import Discord.App.AppInstance;
import Discord.commands.*;
import Discord.playerHandlers.*;
import Discord.twitchIntegration.ChatBotListener;
import com.github.topi314.lavalyrics.LyricsManager;
import com.seb.io.Reader;
import com.seb.io.Writer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import io.netty.buffer.ByteBuf;
import moe.kyokobot.koe.*;
import moe.kyokobot.koe.media.OpusAudioFrameProvider;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_OPUS;

/**
 * The class for every server
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class Server {
    //TODO: test lag, if not good enough learn how to implement the other stuff (https://github.com/KyokoBot/koe/tree/master/ext-udpqueue)

    Koe koe;
    KoeClient koeClient;

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

    /**
     * <p>Getter for the field <code>guild</code>.</p>
     *
     * @return a {@link net.dv8tion.jda.api.entities.Guild} object
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * <p>Getter for the field <code>streamer</code>.</p>
     *
     * @return a {@link net.dv8tion.jda.api.entities.Member} object
     */
    public Member getStreamer() {
        return streamer;
    }

    /**
     * <p>Setter for the field <code>streamer</code>.</p>
     *
     * @param streamer a {@link net.dv8tion.jda.api.entities.Member} object
     */
    public void setStreamer(Member streamer) {
        this.streamer = streamer;
    }

    private Member streamer;
    private final Guild guild;
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final AudioPlayer player = audioPlayerManager.createPlayer();
    private final AudioManager audioManager;
    private final TrackScheduler trackScheduler;
    private final DisconnectTimer dc;

    /**
     * <p>Getter for the field <code>tracks</code>.</p>
     *
     * @return an array of {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack} objects
     */
    public AudioTrack[] getTracks() {
        return tracks;
    }

    private final AudioTrack[] tracks = new AudioTrack[5];
    private final LyricsManager lyricsManager = new LyricsManager();
    /**
     * members connected to the app
     */
    public final HashMap<UUID, Member> members = new HashMap<>();
    private final ArrayList<AppInstance> appInstances = new ArrayList<>();

    /**
     * <p>Getter for the field <code>appInstances</code>.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<AppInstance> getAppInstances() {
        return appInstances;
    }

    /**
     * <p>Getter for the field <code>chatBotListener</code>.</p>
     *
     * @return a {@link Discord.twitchIntegration.ChatBotListener} object
     */
    public ChatBotListener getChatBotListener() {
        return chatBotListener;
    }

    private final ChatBotListener chatBotListener = new ChatBotListener(this);
    private final static YoutubeAudioSourceManager ytsrc = new YoutubeAudioSourceManager(true, new TvHtml5Embedded(), new AndroidMusicWithThumbnail(), new IosWithThumbnail(), new Music());
    static {
        ytsrc.useOauth2(Reader.read(new File("youtubetoken.env")), true);
    }

    /**
     * Server creation
     *
     * @param guild to get needed info from
     * @throws java.io.IOException if any.
     */
    public Server(Guild guild) throws IOException {
        this.guild = guild;
        guildId = guild.getId();
        volume = readVolume();
        koe = Koe.koe();
        koeClient = koe.newClient(guild.getJDA().getSelfUser().getIdLong());
        //web issues for a lot of songs
        // new AndroidMusicWithThumbnail() works partly for songs and for spotify
        // new IOSWithThumbnail works for yt vids
        // music for ytmusic search
        //tvhtml5 for ytsearch
        //i think thats it for now? seems like web was broken, replaced with ios
        //can create new ClientOptions for clients to disable certain features when broken, need working ones for everything tho

        audioPlayerManager.registerSourceManager(ytsrc);
        //???? other clients work, i guess i just do this for now
        SpotifySourceManager spsrc = new SpotifySourceManager(null, NewMain.clientid, NewMain.clientsecret, "de", audioPlayerManager, NewMain.spdc);
        audioPlayerManager.registerSourceManager(spsrc);
        lyricsManager.registerLyricsManager(spsrc);
        /*
        I understand now
        The SpotifySourceManager just searches the spotify songs on youtube and plays those
        what a nice way to do it man, wtf am i witnessing lol
        can play local files too tho if wanted, not integrated rn
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
            Writer.write("100", f);
        }
        return Integer.parseInt(Reader.read(f));
    }

    /**
     * <p>join.</p>
     *
     * @param channel a {@link net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion} object
     * @return a {@link Discord.JoinStates} object
     */
    public JoinStates join (AudioChannelUnion channel) {
        //TODO: Monitor, might have some issues doing it regularly or something, sometimes get rate limits out of nowhere
        if (player.getPlayingTrack() == null) {
            dc.startTimer();
        }
        if (channel == null) {
            return JoinStates.NOTINVOICE;
        }
        if (!guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT)) {
            // The bot does not have permission to join any voice channel. Don't forget the .queue()!
            return JoinStates.NOPERMS;
        }
        // Creates a variable equal to the channel that the user is in.

        VoiceChannel connectedChannel = channel.asVoiceChannel();
        // Checks if they are in a channel -- not being in a channel means that the variable = null.
        // Gets the audio manager.
        if (audioManager.isConnected() && audioManager.getConnectedChannel().asVoiceChannel().equals(connectedChannel)) {
            return JoinStates.ALREADYCONNECTED;
        }
        var conn = koeClient.createConnection(Long.parseLong(guildId));
        conn.setAudioSender(new AudioSender(player, conn));
        audioManager.openAudioConnection(connectedChannel);
        // Obviously people do not notice someone/something connecting.
        appInstances.forEach(instance -> instance.setChannel(channel.getJumpUrl()));
        return JoinStates.JOINED;
    }

    /**
     * <p>leave.</p>
     *
     * @return a boolean
     */
    public boolean leave() {
        if (koeClient.getConnection(guild.getIdLong()) == null) {
            return false;
        }
        // Disconnect from the channel.
        audioManager.closeAudioConnection();
        // Notify the user.
        player.stopTrack();
        dc.stopTimer();
        if (player.isPaused()) player.setPaused(false);
        if (streamer != null) {
            chatBotListener.disconnect(false);
            streamer = null;
        }
        trackScheduler.repeating = RepeatState.NO_REPEAT;
        appInstances.forEach(AppInstance::setIdlePresence);
        return true;
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
            case "appconnect" -> new AppConnectionCommand(event, this);
            case "togglerequests" -> new ToggleRequestCommand(event, this);
            case "previous" -> new PrevCommand(event, this);
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

    public void onVoiceServerUpdate(@NotNull VoiceDispatchInterceptor.VoiceServerUpdate update) {

        var conn = koeClient.getConnection(update.getGuildIdLong());
        if (conn != null) {
            var info = new VoiceServerInfo(
                    update.getSessionId(),
                    update.getEndpoint(),
                    update.getToken()
            );
            conn.connect(info);
        }
    }

    public boolean onVoiceStateUpdate(@NotNull VoiceDispatchInterceptor.VoiceStateUpdate update) {
        if (update.getVoiceState().getIdLong() == guild.getJDA().getSelfUser().getIdLong() && update.getChannel().getIdLong() == 0) {
            koeClient.destroyConnection(update.getGuildIdLong());
        }
        return true;
    }

    private static class AudioSender extends OpusAudioFrameProvider {
        private final AudioPlayer player;
        private final MutableAudioFrame frame;
        private final ByteBuffer frameBuffer;

        AudioSender(AudioPlayer player, MediaConnection connection) {
            super(connection);
            this.player = player;
            this.frame = new MutableAudioFrame();
            this.frameBuffer = ByteBuffer.allocate(DISCORD_OPUS.maximumChunkSize());
            frame.setBuffer(frameBuffer);
            frame.setFormat(DISCORD_OPUS);
        }

        @Override
        public boolean canProvide() {
            return player.provide(frame);
        }

        @Override
        public void retrieveOpusFrame(ByteBuf targetBuffer) {
            targetBuffer.writeBytes(frameBuffer.array(), 0, frame.getDataLength());
        }
    }
}
