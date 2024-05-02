package Discord;

import Discord.commands.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.*;

public class Server {

    private final Guild guild;

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getGuildId() {
        return guildId;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public DisconnectTimer getDc() {
        return dc;
    }

    private final String guildId;
    private int volume;

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final AudioPlayer player = audioPlayerManager.createPlayer();
    private final AudioManager audioManager;
    private final TrackScheduler trackScheduler;
    private final AudioPlayerHandler audioPlayerHandler = new AudioPlayerHandler(player);
    private final DisconnectTimer dc;

    public Server(Guild guild) {
        this.guild = guild;
        guildId = guild.getId();
        volume = readVolume();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        this.audioManager = guild.getAudioManager();
        trackScheduler = new TrackScheduler(this);
        player.addListener(trackScheduler);
        dc = new DisconnectTimer(audioManager);
        Thread dcThread = new Thread(dc);
        dcThread.start();
        player.setVolume(volume);
    }

    private int readVolume() {
        File f = new File("volumes/" + guildId);
        f.getParentFile().mkdirs();
        if (!f.exists()) {
            NewMain.write("100", f);
        }
        return Integer.parseInt(NewMain.read(f).toString());
    }

    public JoinStates join (SlashCommandInteractionEvent event) {
        if (!guild.getSelfMember().hasPermission(event.getGuildChannel(), Permission.VOICE_CONNECT)) {
            // The bot does not have permission to join any voice channel. Don't forget the .queue()!
            return JoinStates.NOPERMS;
        }
        // Creates a variable equal to the channel that the user is in.
        if (event.getMember().getVoiceState().getChannel() == null) {
            return JoinStates.NOTINVOICE;
        }
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

    public void onSlashCommandInteraction (SlashCommandInteractionEvent event) {
        String s = event.getName();
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
        }
    }

    public void onButtonInteraction (ButtonInteractionEvent event) {
        new PlayCommand(event, this);
    }
}
