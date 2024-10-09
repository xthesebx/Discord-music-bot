package Discord.App;

import Discord.Server;
import com.hawolt.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class AppListener {

    ServerSocket serverSocket;
    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;
    public static HashMap<UUID, Server> auth = new HashMap<>();

    public AppListener() throws IOException {
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
                            Server server = auth.get(UUID.fromString(s));
                            AppInstance instance = new AppInstance(clientSocket, server, UUID.fromString(s), out);
                            server.getAppInstances().add(instance);
                            out.println("yes");
                            new Thread(instance).start();
                            auth.remove(UUID.fromString(s));
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