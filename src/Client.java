import java.io.*;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

            // Read server prompt
            String serverMessage = in.readLine();
            System.out.println(serverMessage);

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String name = userInput.readLine(); // send name
            out.write(name);
            out.newLine();
            out.flush();

            System.out.println("Connected to server as " + name + "!");

            // Start input handler thread for sending messages
            new Thread(new InputHandler()).start();

            // Listen for server messages
            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }

        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = userInput.readLine();
                    if (message.equals("/quit")) {
                        out.write(message);
                        out.newLine();
                        out.flush();
                        shutdown();
                        break;
                    } else {
                        out.write(message);
                        out.newLine();
                        out.flush();
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Client()).start();
    }
}
