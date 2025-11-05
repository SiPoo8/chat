import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final CopyOnWriteArrayList<ConnectionHandler> connections = new CopyOnWriteArrayList<>();
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            System.out.println("Server started on port 9999...");

            while (!done) {
                Socket connection = server.accept();
                ConnectionHandler handler = new ConnectionHandler(connection);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdownServer();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler client : connections) {
            client.sendMessage(message);
        }
    }

    public void shutdownServer() {
        done = true;
        try {
            if (server != null && !server.isClosed()) server.close();
            for (ConnectionHandler client : connections) {
                client.shutdownClient();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClient(ConnectionHandler client) {
        connections.remove(client);
        System.out.println(client.getName() + " disconnected.");
    }

    class ConnectionHandler implements Runnable {
        private final Socket connection;
        private BufferedReader in;
        private BufferedWriter out;
        private String name;

        public ConnectionHandler(Socket connection) {
            this.connection = connection;
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

                // Ask for name
                out.write("Please enter your name:");
                out.newLine();
                out.flush();

                name = in.readLine();
                System.out.println(name + " joined the server");
                broadcast("Welcome " + name);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/name")) {
                        String[] parts = message.split(" ", 2);
                        if (parts.length == 2) {
                            String oldName = name;
                            name = parts[1];
                            broadcast(oldName + " changed name to " + name);
                            out.write("Successfully changed name to " + name);
                            out.newLine();
                            out.flush();
                        } else {
                            out.write("No name provided");
                            out.newLine();
                            out.flush();
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(name + " left the server");
                        shutdownClient();
                        break;
                    } else {
                        broadcast(name + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdownClient();
            }
        }

        public void sendMessage(String message) {
            try {
                out.write(message);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                shutdownClient();
            }
        }

        public void shutdownClient() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (!connection.isClosed()) connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                removeClient(this);
            }
        }
    }

    public static void main(String[] args) {
        Thread serverThread = new Thread(new Server());
        serverThread.start();
    }
}
