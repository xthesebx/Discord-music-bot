package Discord.commands;

import Discord.DisconnectTimer;
import Discord.Server;
import Discord.TrackScheduler;
import com.hawolt.logger.Logger;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * command /play
 */
public class PlayCommand extends BasicCommand {

    private final String[] urls = new String[5];
    AudioManager audioManager;
    private final String link;
    private final TrackScheduler trackScheduler;
    private final DisconnectTimer dc;

    /**
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public PlayCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.trackScheduler = server.getTrackScheduler();
        this.audioManager = server.getAudioManager();
        this.dc = server.getDc();
        event.deferReply().queue();
        OptionMapping optionMapping = event.getOption("name");
        assert optionMapping != null;
        link = optionMapping.getAsString();
        if (!audioManager.isConnected() || !audioManager.getConnectedChannel().getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
            switch (server.join(event)) {
                case NOTINVOICE -> {
                    event.reply("You must be in a Voice-Channel for me to join your Channel!").queue();
                    return;
                }
                case NOPERMS -> {
                    event.reply("I do not have permissions to join a voice channel!").queue();
                    return;
                }
            }
        }
        play(event);
    }

    public PlayCommand(ButtonInteractionEvent event, Server server) {
        super(event, server);
        this.trackScheduler = server.getTrackScheduler();
        this.dc = server.getDc();
        link = urls[Integer.parseInt(event.getButton().getId())];
        play(event);
    }

    public void play(GenericInteractionCreateEvent genericEvent) {
        boolean eventType;
        eventType = genericEvent instanceof ButtonInteractionEvent;
        server.getAudioPlayerManager().loadItem(link, new AudioLoadResultHandler() {
            String text;

            @Override
            public void trackLoaded (AudioTrack audioTrack) {
                trackScheduler.queue(audioTrack);
                dc.stopTimer();
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
}
