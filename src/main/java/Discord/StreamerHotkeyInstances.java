package Discord;

import com.hawolt.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class StreamerHotkeyInstances implements Runnable {

    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;
    Server server;

    public StreamerHotkeyInstances(Socket clientSocket, Server server) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream());
        this.server = server;
        Logger.error("connected");
    }

    @Override
    public void run() {
        String s;
        while (true) {
            try {
                if (!((s = in.readLine()) != null)) break;
                switch (s) {
                    case "playpause" -> {
                        if (server.getPlayer().isPaused()) server.getPlayer().setPaused(false);
                        else server.getPlayer().setPaused(true);
                    }
                    case "nexttrack" -> {
                        server.getTrackScheduler().nextTrack();
                    }
                }
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    close();
                }
            }
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {

        }
    }
}
