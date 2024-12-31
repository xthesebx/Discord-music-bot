package Discord.twitchIntegration;


import Discord.Server;
import Discord.playerHandlers.PlayMethods;
import com.hawolt.logger.Logger;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * <p>ChatBotListener class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class ChatBotListener implements Runnable {


    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Server server;
    private String channel;
    private boolean reconnect = true;
    /**
     * {@code requests} toggle to enable requests
     */
    public boolean requests = true;
    /**
     * constructor for the tcp listener
     *
     * @param server discord server regarding streamer mode
     */
    public ChatBotListener(Server server) {
        this.server = server;
    }

    /**
     * connects to tcp socket
     *
     * @param channel the twitch chat required
     * @throws java.io.IOException because of sockets you know
     */
    public void connect(String channel) throws IOException {
        this.channel = channel;
        socket = new Socket("127.0.0.1", 42069);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println(channel);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        outerloop:
        while(true) {
            try {
                String s = in.readLine();
                if (s.equals("song?")) {
                    if (server.getPlayer().getPlayingTrack() == null)
                        out.println("no Song currently playing");
                    else out.println(server.getPlayer().getPlayingTrack().getInfo().uri);
                } else if (requests)
                    PlayMethods.play(s, server);
                else out.println("requests are currently disabled");
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    if (reconnect) {
                        disconnect(true);
                        for (int i = 1; i < 6; i++) {
                            try {
                                Thread.sleep(i * 5000);
                                Logger.error("trying to reconnect");
                                connect(channel);
                                continue outerloop;
                            } catch (InterruptedException | IOException ignored) {
                            }
                        }
                    }
                    server.setStreamer(null);
                    return;
                } else {
                    Logger.error(e);
                }
            }
        }
    }

    /**
     * disconnects from the socket
     */
    public void disconnect(boolean reconnect) {
        this.reconnect = reconnect;
        out.println("close");
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * <p>addedRequest.</p>
     *
     * @param track a {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack} object
     */
    public void addedRequest(AudioTrack track) {
        out.println("added \"" + track.getInfo().title + "\" to queue");
    }
    public void print(String s) {
        out.println(s);
    }
}
