package Discord.App;

import Discord.Server;
import com.hawolt.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.UUID;

/**
 * <p>AppListener class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class AppListener {

    private final ServerSocket serverSocket;
    /** Constant <code>auth</code> */
    public static HashMap<UUID, Server> auth = new HashMap<>();

    /**
     * <p>Constructor for AppListener.</p>
     *
     * @throws java.io.IOException if any.
     */
    public AppListener() throws IOException {
        serverSocket = new ServerSocket(4269);
        new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(() -> {
                            try {
                                Logger.debug(clientSocket.getInetAddress().getHostAddress());
                                clientSocket.setSoTimeout(30000);
                                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                                try {
                                    String s = in.readLine();
                                    if (s == null || s.isEmpty()) {
                                        return;
                                    }
                                    if (auth.containsKey(UUID.fromString(s))) {
                                        Server server = auth.get(UUID.fromString(s));
                                        AppInstance instance = new AppInstance(clientSocket, server, UUID.fromString(s), out);
                                        server.getAppInstances().add(instance);
                                        out.println(server.getGuild().getName());
                                        new Thread(instance).start();
                                    } else {
                                        out.println("no");
                                        out.close();
                                        in.close();
                                        clientSocket.close();
                                    }
                                } catch (IllegalArgumentException e) {
                                    Logger.error(e);
                                    out.println("no");
                                    out.close();
                                    in.close();
                                    clientSocket.close();
                                } catch (SocketException e) {
                                    Logger.debug("reset socket on connect I guess");
                                }
                            } catch (IOException ex) {
                                Logger.error(ex);
                            }
                        }).start();
                    } catch (IOException ex) {
                        Logger.error(ex);
                    }
                }
        }).start();
    }
}
