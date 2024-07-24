import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class Main {
    static String directory;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--directory")) {
            directory = args[1];
//            System.out.println(directory);
        }
        try {
            ServerSocket serverSocket = new ServerSocket(4221);

            // since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

          while (true) {
            Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
            System.out.println("accepted new connection");

            // Create a new thread for each client
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            new Thread(clientHandler).start();
          }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    public static String prepareHeaders(Headers resH, int contentLength) {
        resH.appendBody("Content-Type: text/plain");
        resH.appendBody("Content-Length: " + contentLength);
        return resH.getHeaders();
    }
}
