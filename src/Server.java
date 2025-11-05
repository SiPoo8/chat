import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }


    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            System.out.println("Server started on port 9999...");
            while (!done) {
                Socket connection = server.accept();
                ConnectionHandler Handler = new ConnectionHandler(connection);
                connections.add(Handler);
                pool.execute(Handler);
            }
        } catch (Exception e) {
            shutdown();
        }

    }

    public void broadcast(String message) {
        for (ConnectionHandler connection : connections) {
            connection.sendMessage(message + "\n\t");
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler connection : connections) {
                connection.shutdown();
                connections.remove(connection);
            }
        } catch (IOException e) {
            // Not Handleble
        }
    }


    class ConnectionHandler implements Runnable {

        private final Socket connection;
        private BufferedReader in;
        private BufferedWriter out;
        private String name;


        public ConnectionHandler(Socket connection) {
            this.connection = connection;
        }


        @Override
        public void run() {
            try {
                out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                out.write("Please enter your name: " + "\n\t");
                out.flush();

                name = in.readLine();
                System.out.println(name + " joined the server");
                broadcast("Welcome " + name);
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/name")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(name + " renamed themselves to" + messageSplit[1]);
                            System.out.println(name + " renamed themselves to" + messageSplit[1]);
                            name = messageSplit[1];
                            out.write("Successfully changed name to" + messageSplit[1]);
                            out.flush();
                        } else {
                            out.write("No name provided");
                            out.flush();
                        }

                    } else if (message.startsWith("/quit")) {
                        broadcast(name + "left the server" );
                        shutdown();
                    } else {
                        broadcast(name + ": " + message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            try {
                out.write(message);
                out.flush();
            } catch (IOException e) {
                // ignore
            }
        }

        public void shutdown() {
            try {
                in.close();
                out.close();

                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (IOException e) {
                // Cant be handled
            }

        }


    }


    public static void main(String[] args) {
        Thread serverThread = new Thread(new Server());
        serverThread.start();

    }


}
