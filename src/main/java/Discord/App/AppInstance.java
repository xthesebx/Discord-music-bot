package Discord.App;

import Discord.PlayMethods;
import Discord.Server;
import Discord.commands.ShuffleCommand;
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

public class AppInstance implements Runnable {

    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;
    Server server;
    UUID uuid;

    public AppQueue getAppQueue() {
        return appQueue;
    }

    AppQueue appQueue;

    public AppInstance(Socket clientSocket, Server server, UUID uuid, PrintWriter out) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = out;
        this.server = server;
        this.uuid = uuid;
        this.appQueue = new AppQueue(server, this);
        AppQueue.debouncer.debounce("appqueue", () ->
                appQueue.initQueue(), 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        String s;
        try {
            while ((s = in.readLine()) != null) {
                if (s.startsWith("play ")) {
                    PlayMethods.playApp(s.substring(s.indexOf(" ") + 1), server);
                } else if (s.startsWith("{\"delete")) {
                    JSONObject object = new JSONObject(s);
                    JSONArray array = object.getJSONArray("delete");
                    server.getTrackScheduler().removeFromQueue(array);
                } else if (s.startsWith("move ")) {
                    String fromString = s.substring(s.indexOf(" ") + 1);
                    int from = Integer.parseInt(fromString.substring(0, fromString.indexOf(" ")));
                    int to  = Integer.parseInt(fromString.substring(fromString.indexOf(" ") + 1));
                    server.getTrackScheduler().move(from, to);

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
                            server.join(server.members.get(uuid));
                        }
                        case "leave" -> {
                            server.leave();
                        }
                        case "shuffle" -> {
                            ShuffleCommand.shuffle(server);
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (e instanceof SocketException) {
                close();
            }
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            server.members.remove(uuid);
            server.getAppInstances().remove(this);
        } catch (IOException e) {

        }
    }
}
