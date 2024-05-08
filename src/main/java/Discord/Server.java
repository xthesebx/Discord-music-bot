package Discord;

import Discord.commands.*;
import com.hawolt.logger.Logger;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private final String guildId;
    private int volume;
    private final Guild guild;
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final AudioPlayer player = audioPlayerManager.createPlayer();
    private final AudioManager audioManager;
    private final TrackScheduler trackScheduler;
    private final AudioPlayerHandler audioPlayerHandler = new AudioPlayerHandler(player);
    private final DisconnectTimer dc;
    private final String[] urls = new String[5];

    /**
     * Server creation
     *
     * @param guild to get needed info from
     */
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

    /**
     * creating the Command responses for the SlashCommandInteractionEvents
     *
     * @param event the event coming from the newMain
     */
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
            case "skip" -> new SkipCommand(event, this);
        }
    }

    /**
     * creating the Command responses for the ButtonInteractionEvents
     *
     * @param event the event coming from the newMain
     */
    public void onButtonInteraction (ButtonInteractionEvent event) {
        event.getMessage().delete().queue();
        event.deferReply().queue();
        play(urls[Integer.parseInt(event.getButton().getId())], event);
    }

    /**
     * to play the actual youtube thingy
     *
     * @param link link or songtitle to search for/play
     * @param genericEvent is the event, either buttoninteraction or slashcommandinteraction
     */
    public void play(String link, GenericInteractionCreateEvent genericEvent) {
        boolean eventType;
        eventType = genericEvent instanceof ButtonInteractionEvent;
        audioPlayerManager.loadItem(link, new AudioLoadResultHandler() {
            String text;

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                trackScheduler.queue(audioTrack);
                dc.stopTimer();
                text = "```Added " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + " to Queue```";
                if (!eventType) ((SlashCommandInteractionEvent) genericEvent).getHook().editOriginal(text).queue();
                else {
                    ((ButtonInteractionEvent) genericEvent).getHook().editOriginal(new MessageEditBuilder().setContent(text).build()).queue();
                }
            }

            @Override
            public void playlistLoaded (AudioPlaylist audioPlaylist) {
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    trackScheduler.queue(track);
                    text = "```Added " + track.getInfo().title + " by " + track.getInfo().author + " to Queue```";
                    if (!eventType) ((SlashCommandInteractionEvent) genericEvent).getHook().editOriginal(text).queue();
                    else ((ButtonInteractionEvent) genericEvent).getHook().editOriginal(new MessageEditBuilder().setContent(text).setReplace(true).build()).queue();
                }
                dc.stopTimer();
            }

            @Override
            public void noMatches() {
                String[][] ytResults = searchYT(link);
                int i = 0;
                Button[] rows = new Button[5];
                for (String[] s : ytResults) {
                    urls[i] = s[0];
                    String title = s[1];
                    if (title.length() > 80)
                        rows[i] = Button.primary(String.valueOf(i), s[1].substring(0, 79));
                    else
                        rows[i] = Button.primary(String.valueOf(i), s[1]);
                    i++;
                }
                MessageEditData messageEditData = new MessageEditBuilder().setActionRow(rows).setContent("Which one?").build();
                ((SlashCommandInteractionEvent) genericEvent).getHook().editOriginal(messageEditData).queue();
            }

            @Override
            public void loadFailed (FriendlyException e) {
                Logger.error(e);
                text = e.getMessage();
                if (!eventType) ((SlashCommandInteractionEvent) genericEvent).getHook().editOriginal(text).queue();
                else ((ButtonInteractionEvent) genericEvent).getHook().editOriginal(new MessageEditBuilder().setContent(text).setReplace(true).build()).queue();
            }
        });
    }

    /**
     * to search YT for the songname
     * @param songitle the title to search for
     * @return returns link, title and uploader channel i think?
     */
    private String[][] searchYT (String songitle) {
        try {
            songitle = songitle.replaceAll(" ", "+");
            URL url = new URL("https://www.youtube.com/results?search_query=" + songitle);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            BufferedInputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            byte[] dataBuffer = new byte[1024];
            StringBuilder r = new StringBuilder();
            while (inputStream.read(dataBuffer, 0, 1024) != -1) {
                r.append(new String(dataBuffer));
            }
            String[][] list = new String[5][2];
            r = new StringBuilder(r.toString().replaceAll("\\\\u0026", "&"));
            r = new StringBuilder(r.toString().replaceAll("&amp;", "&"));
            String temp = r.toString();
            for (int i = 0; i < 5; i++) {
                temp = temp.substring(temp.indexOf("/watch?"));
                if (i > 0) {
                    while (list[i - 1][0].contains(temp.substring(0, temp.indexOf("&pp")))) {
                        temp = temp.substring(temp.indexOf("\""));
                        temp = temp.substring(temp.indexOf("/watch?"));
                    }
                }
                list[i][0] = "https://www.youtube.com" + temp.substring(0, temp.indexOf("\""));
                URL tempurl = new URL(list[i][0]);
                HttpURLConnection con = (HttpURLConnection) tempurl.openConnection();
                inputStream = new BufferedInputStream(con.getInputStream());
                dataBuffer = new byte[1024];
                StringBuilder t = new StringBuilder();
                while (inputStream.read(dataBuffer, 0, 1024) != -1) {
                    t.append(new String(dataBuffer));
                }
                t = new StringBuilder(t.toString().replaceAll("\\\\u0026", "&"));
                t = new StringBuilder(t.toString().replaceAll("&amp;", "&"));
                t = new StringBuilder(t.toString().replaceAll("&#39;", "'"));
                list[i][1] = t.substring(t.indexOf("<title>") + 7, t.indexOf("</title>") - 10);
                temp = temp.substring(temp.indexOf("\""));
            }
            inputStream.close();
            return (list);
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }
}
