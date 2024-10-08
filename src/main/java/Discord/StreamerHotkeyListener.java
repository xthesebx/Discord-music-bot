package Discord;

import com.hawolt.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class StreamerHotkeyListener {

    ServerSocket serverSocket;
    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;
    public static HashMap<UUID, Server> auth = new HashMap<>();

    public StreamerHotkeyListener() throws IOException {
        serverSocket = new ServerSocket(4269);
        new Thread(() -> {
            try {
                while (true) {
                    clientSocket = serverSocket.accept();
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    String s = in.readLine();
                    try {
                        if (auth.containsKey(UUID.fromString(s))) {
                            new Thread(new StreamerHotkeyInstances(clientSocket, auth.get(UUID.fromString(s)))).start();
                            auth.remove(UUID.fromString(s));
                            out.println("yes");
                        } else {
                            out.println("no");
                            out.close();
                            in.close();
                            clientSocket.close();
                        }
                    } catch (IllegalArgumentException e) {
                        out.println("no");
                        out.close();
                        in.close();
                        clientSocket.close();
                    }
                }
            } catch (IOException e) {
                Logger.error(e);
            }
        }).start();
    }
}
