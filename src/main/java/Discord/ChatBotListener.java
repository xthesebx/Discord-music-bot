package Discord;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class ChatBotListener {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Server server;

    /**
     * constructor for the tcp listener
     * @param server discord server regarding streamer mode
     */
    public ChatBotListener(Server server) {
        this.server = server;
    }

    /**
     * connects to tcp socket
     * @param channel the twitch chat required
     * @throws IOException because of sockets you know
     */
    public void connect(String channel) throws IOException {
        try {
            socket = new Socket("127.0.0.1", 42069);
        } catch (ConnectException e) {
            server.setStreamer(null);
        }
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println(channel);
        while(true) {
            try {
                String s = in.readLine();
                if (s.equals("song?")) {
                    out.println(server.getPlayer().getPlayingTrack().getInfo().uri);
                } else
                    server.play(s);
            } catch (SocketException e) {
                disconnect();
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(i * 1000);
                    } catch (InterruptedException ignored) {}
                    connect(channel);
                }
                server.setStreamer(null);
                return;
            }
        }
    }

    /**
     * disconnects from the socket
     */
    public void disconnect() {
        out.println("close");
        out.close();
        try {
            in.close();
            socket.close();
        } catch (IOException ignored) {}
    }
}
