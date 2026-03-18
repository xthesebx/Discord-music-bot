package Discord;

import Discord.App.AppInstance;
import Discord.commands.*;
import Discord.playerHandlers.*;
import Discord.twitchIntegration.ChatBotListener;
import com.hawolt.logger.Logger;
import com.seb.io.Reader;
import com.seb.io.Writer;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.VoiceState;
import dev.lavalink.youtube.clients.*;
import moe.kyokobot.koe.*;
import moe.kyokobot.koe.codec.udpqueue.UdpQueueFramePollerFactory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;


/**
 * The class for every server
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class Server {

    Track[] tracks = new Track[5];


    private final KoeClient koeClient;

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
    public Long getGuildId() {
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
     * getter for Dc
     *
     * @return the Dc
     */
    public DisconnectTimer getDc() {
        return dc;
    }


    private final Long guildId;
    private int volume;

    public int getVolume() {
        return volume;
    }

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
    private final AudioManager audioManager;
    private final TrackScheduler trackScheduler;
    private final DisconnectTimer dc;
    private final LavalinkClient lavalink;

    /**

    private final LyricsManager lyricsManager = new LyricsManager();
    /**
     * members connected to the app
     */
    public final HashMap<UUID, String> members = new HashMap<>();
    private final HashMap<UUID, AppInstance> appInstances = new HashMap<>();

    /**
     * <p>Getter for the field <code>appInstances</code>.</p>
     *
     * @return a {@link java.util.List} object
     */
    public HashMap<UUID, AppInstance> getAppInstances() {
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
    //web issues for a lot of songs
    // new AndroidMusicWithThumbnail() works partly for songs and for spotify
    // new IOSWithThumbnail works for yt vids
    // music for ytmusic search
    //tvhtml5 for ytsearch
    //i think thats it for now? seems like web was broken, replaced with ios
    //can create new ClientOptions for clients to disable certain features when broken, need working ones for everything tho
    /**
     * Server creation
     *
     * @param guild to get needed info from
     * @throws java.io.IOException if any.
     */
    public Server(Guild guild, LavalinkClient lavalink) throws IOException {
        this.lavalink = lavalink;
        this.guild = guild;
        guildId = guild.getIdLong();
        volume = readVolume();
        Koe koe = Koe.koe(KoeOptions.builder().setFramePollerFactory(new UdpQueueFramePollerFactory()).create());
        koeClient = koe.newClient(guild.getJDA().getSelfUser().getIdLong());
        /*
        can play local files too if wanted, not integrated rn
         */
        this.audioManager = guild.getAudioManager();
        trackScheduler = new TrackScheduler(this);
        dc = new DisconnectTimer(this);
        Thread dcThread = new Thread(dc);
        dcThread.start();
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
        if (getPlayer().isEmpty() || getPlayer().get().getTrack() == null) {
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
        lavalink.getOrCreateLink(guildId);
        koeClient.createConnection(guildId);


        try {
            audioManager.openAudioConnection(connectedChannel);
        } catch (InsufficientPermissionException e) {
            return JoinStates.CHANNELFULL;
        }
        // Obviously people do not notice someone/something connecting.
        appInstances.values().forEach(instance -> instance.setChannel(channel.getJumpUrl()));
        return JoinStates.JOINED;
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(
                NewMain.client.getLinkIfCached(guildId)
        );
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return getLink().map(Link::getCachedPlayer);
    }

    VoiceState lastVoiceState;
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
        koeClient.getConnection(guild.getIdLong()).disconnect();
        // Notify the user.
        getPlayer().ifPresent(player -> {
            player.setTrack(null);
            player.setPaused(false);
        });
        dc.stopTimer();
        if (streamer != null) {
            chatBotListener.disconnect(false);
            streamer = null;
        }
        trackScheduler.repeating = RepeatState.NO_REPEAT;
        appInstances.values().forEach(instance -> instance.getAppQueue().repeat());
        appInstances.values().forEach(AppInstance::setIdlePresence);
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
            Track track = tracks[Integer.parseInt(event.getButton().getCustomId())];
            if (track != null) {
                trackScheduler.queue(track);
                event.getHook().editOriginal("```Added " + track.getInfo().getTitle() + " by " + track.getInfo().getAuthor() + " to queue```").queue();
            } else event.getHook().editOriginal("```Search is no longer available due to a bot restart```").queue();
        } catch (NullPointerException e) {
            event.getHook().editOriginal("```Button is from old Bot Task, cant execute it```").queue();
        }
    }

    public Track[] getTracks() {
        return tracks;
    }

    public VoiceState voiceState;

    public void onVoiceServerUpdate(@NotNull VoiceDispatchInterceptor.VoiceServerUpdate update) {
        voiceState = new VoiceState(update.getToken(), update.getEndpoint(), update.getSessionId(), guild.getSelfMember().getVoiceState().getChannel().getId());
        lastVoiceState = voiceState;
        Logger.error(lavalink.getLinkIfCached(getGuildId()));
        lavalink.getOrCreateLink(guildId).onVoiceServerUpdate(voiceState);
        getPlayer().ifPresent(player -> {
            player.setPaused(true);
        });
        lavalink.getOrCreateLink(guildId);
        var conn = koeClient.getConnection(update.getGuildIdLong());
        if (conn != null) {
            var info = new VoiceServerInfo(
                    update.getSessionId(),
                    update.getEndpoint(),
                    update.getToken()
            );
            conn.connect(info);
            conn.startAudioFramePolling();
        }
        getPlayer().ifPresent(player -> player.setPaused(false));

        trackScheduler.ready = true;
    }

    public boolean onVoiceStateUpdate(@NotNull VoiceDispatchInterceptor.VoiceStateUpdate update) {
        if (update.getVoiceState().getIdLong() == guild.getJDA().getSelfUser().getIdLong() && update.getChannel().getIdLong() == 0) {
            koeClient.destroyConnection(update.getGuildIdLong());
        }
        return true;
    }
}
