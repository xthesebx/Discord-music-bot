package Discord.App;

import Discord.commands.VolumeCommand;
import Discord.playerHandlers.PlayMethods;
import Discord.Server;
import Discord.commands.ShuffleCommand;
import Discord.commands.StreamerModeCommands;
import Discord.playerHandlers.RepeatState;
import com.hawolt.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>AppInstance class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class AppInstance implements Runnable {

    public Socket getClientSocket() {
        return clientSocket;
    }

    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;
    Server server;
    UUID uuid;

    /**
     * <p>Getter for the field <code>appQueue</code>.</p>
     *
     * @return a {@link Discord.App.AppQueue} object
     */
    public AppQueue getAppQueue() {
        return appQueue;
    }

    AppQueue appQueue;

    /**
     * <p>Constructor for AppInstance.</p>
     *
     * @param clientSocket a {@link java.net.Socket} object
     * @param server a {@link Discord.Server} object
     * @param uuid a {@link java.util.UUID} object
     * @param out a {@link java.io.PrintWriter} object
     * @throws java.io.IOException if any.
     */
    public AppInstance(Socket clientSocket, Server server, UUID uuid, PrintWriter out) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = out;
        this.server = server;
        this.uuid = uuid;
        this.appQueue = new AppQueue(server, this);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        if (server.getAudioManager().getConnectedChannel() != null)
            setChannel(server.getAudioManager().getConnectedChannel().getJumpUrl());
        AppQueue.debouncer.debounce("appqueue", () ->
                appQueue.initQueue(), 1, TimeUnit.SECONDS);
        String s;
        try {
            while ((s = in.readLine()) != null) {
                Logger.debug(clientSocket.getInetAddress().getHostAddress() + " : " + s);
                if (s.startsWith("play ")) {
                    server.join(server.members.get(uuid).getVoiceState().getChannel());
                    PlayMethods.playApp(s.substring(s.indexOf(" ") + 1), server);
                } else if (s.startsWith("{\"delete")) {
                    JSONObject object = new JSONObject(s);
                    JSONArray array = object.getJSONArray("delete");
                    server.getTrackScheduler().removeFromQueue(array);
                } else if (s.startsWith("move ")) {
                    String fromString = s.substring(s.indexOf(" ") + 1);
                    int from = Integer.parseInt(fromString.substring(0, fromString.indexOf(" ")));
                    int to = Integer.parseInt(fromString.substring(fromString.indexOf(" ") + 1));
                    server.getTrackScheduler().move(from, to);
                } else if (s.startsWith("streamer ")) {
                    String twitchname = s.substring(s.indexOf(" ") + 1);
                    StreamerModeCommands.setStreamer(server, server.members.get(uuid), twitchname);
                } else if (s.startsWith("volume ")) {
                    String volume = s.substring(s.indexOf(" ") + 1);
                    VolumeCommand.setVolume(server, Integer.parseInt(volume));
                } else {
                    switch (s) {
                        case "playpause" -> {
                            if (server.getPlayer().isPaused()) server.getPlayer().setPaused(false);
                            else server.getPlayer().setPaused(true);
                        }
                        case "nexttrack" -> {
                            server.getTrackScheduler().nextTrack();
                        }
                        case "join" -> {
                            server.join(server.members.get(uuid).getVoiceState().getChannel());
                        }
                        case "leave" -> {
                            server.leave();
                        }
                        case "stop" -> {
                            server.getPlayer().stopTrack();
                            server.getDc().startTimer();
                            if (server.getPlayer().isPaused()) server.getPlayer().setPaused(false);
                            server.getTrackScheduler().repeating = RepeatState.NO_REPEAT;
                            setIdlePresence();
                        }
                        case "shuffle" -> ShuffleCommand.shuffle(server);
                        case "repeat" -> server.getTrackScheduler().toggleRepeat();
                        case "prevtrack" -> server.getTrackScheduler().previousTrack();
                        case "toggle" -> server.getChatBotListener().requests = !server.getChatBotListener().requests;
                    }
                }
            }
        } catch (IOException e) {
            if (e instanceof SocketException) {
                closeClient();
            }
        }
    }

    private void closeClient() {
        close();
        synchronized (server.getAppInstances()) {
            server.getAppInstances().remove(this);
        }
    }

    /**
     * <p>close.</p>
     */
    public void close() {
        try {
            out.println("close");
            clientSocket.close();
            server.members.remove(uuid);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    /**
     * <p>setIdlePresence.</p>
     */
    public void setIdlePresence() {
        out.println("idle");
    }

    /**
     * <p>setChannel.</p>
     *
     * @param channel a {@link java.lang.String} object
     */
    public void setChannel(String channel) {
        out.println("channel " + channel);
    }
}
