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

public class Server {

    public String[] urls = new String[5];
    public final Guild guild;
    public final String guildId;
    public int volume;
    public final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public final AudioPlayer player = audioPlayerManager.createPlayer();
    public final AudioManager audioManager;
    public final TrackScheduler trackScheduler;
    public final AudioPlayerHandler audioPlayerHandler = new AudioPlayerHandler(player);
    public final Thread dcThread;

    public Server(Guild guild) {
        this.guild = guild;
        guildId = guild.getId();
        volume = readVolume();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        this.audioManager = guild.getAudioManager();
        trackScheduler = new TrackScheduler(player, guild.getAudioManager());
        player.addListener(trackScheduler);
        dcThread = new Thread(new DisconnectTimer(audioManager));
        player.setVolume(volume);
    }

    private int readVolume() {
        File f = new File("volumes/" + guildId);
        f.getParentFile().mkdirs();
        if (!f.exists()) {
            write("100", f);
        }
        return Integer.parseInt(newMain.read(f).toString());
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

    public void play(String link, GenericInteractionCreateEvent genericEvent) {
        boolean eventType;
        eventType = genericEvent instanceof ButtonInteractionEvent;
        audioPlayerManager.loadItem(link, new AudioLoadResultHandler() {
            String text;

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                trackScheduler.queue(audioTrack);
                dcThread.interrupt();
                text = "```Added " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + " to Queue```";
                if (!eventType) ((SlashCommandInteractionEvent) genericEvent).getHook().editOriginal(text).queue();
                else ((ButtonInteractionEvent) genericEvent).getHook().editOriginal(new MessageEditBuilder().setContent(text).setReplace(true).build()).queue();
            }

            @Override
            public void playlistLoaded (AudioPlaylist audioPlaylist) {
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    trackScheduler.queue(track);
                    text = "```Added " + track.getInfo().title + " by " + track.getInfo().author + " to Queue```";
                    if (!eventType) ((SlashCommandInteractionEvent) genericEvent).getHook().editOriginal(text).queue();
                    else ((ButtonInteractionEvent) genericEvent).getHook().editOriginal(new MessageEditBuilder().setContent(text).setReplace(true).build()).queue();
                }
                dcThread.interrupt();
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

    private String[][] searchYT (String s) {
        try {
            s = s.replaceAll(" ", "+");
            URL url = new URL("https://www.youtube.com/results?search_query=" + s);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            BufferedInputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            byte[] dataBuffer = new byte[1024];
            StringBuilder r = new StringBuilder();
            while (inputStream.read(dataBuffer, 0, 1024) != -1) {
                r.append(new String(dataBuffer));
            }
            String[][] list = new String[5][3];
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
        play(urls[Integer.parseInt(event.getButton().getId())], event);
    }

    public void write(String text, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            writer.write(text);
            writer.close();
        } catch (IOException ignored) {

        }
    }
}
