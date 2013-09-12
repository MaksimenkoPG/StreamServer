/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package streamserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.Base64;

/**
 *
 * @author pavel
 */
public class StreamServer extends WebSocketServer {

    private static Set<WebSocket> peers = Collections.synchronizedSet(new HashSet<WebSocket>());
    private static File           images_dir = new File("./images");
    private static List<String>   images = new ArrayList<String>();
    
    private static Random         randomGenerator = new Random();

    public StreamServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public StreamServer(InetSocketAddress address) {
        super(address);
    }

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        readFiles();
        WebSocketImpl.DEBUG = false;
        int port = 8080; // 843 flash policy port
        try {
            port = Integer.parseInt(args[ 0]);
        } catch (Exception ex) {
        }
        StreamServer s = new StreamServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());
        System.out.println("Type exit to stop server.");
        System.out.println("Type restart to restart.");

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            s.sendToAll(in);
            if (in.equals("exit")) {
                s.stop();
                break;
            } else if (in.equals("restart")) {
                s.stop();
                s.start();
                break;
            }
        }

    }

    public static void readFiles() {
        File[] files = images_dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        });

        for (File jpg_file : files) {
            images.add( readFile(jpg_file.toString()) );
        }

    }

    public static String readFile(String file_name) {
        String base64String = "";
        try {
            base64String = Base64.encodeFromFile(file_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64String;
    }

    public void sendToAll(String text) {
        synchronized (peers) {
            for (WebSocket c : peers) {
                c.send(text);
            }
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        peers.add(conn);
//        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected.");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        peers.remove(conn);
//        System.out.println(conn + " has left.");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        switch (message) {
            case "get_capture":
                conn.send(images.get(randomGenerator.nextInt(images.size())).toString());
        }
//        System.out.println(conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }
}
