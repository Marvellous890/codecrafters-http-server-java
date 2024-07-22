import java.io.*;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            // System.out.println(line);
            String[] HttpRequest = line.split(" ", 0);
            OutputStream output = clientSocket.getOutputStream();
            if (HttpRequest[1].equals("/")) {
                output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            } else if (HttpRequest[1].startsWith("/echo/")) {
                String content = HttpRequest[1].replace("/echo/", "");
                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + content.length() + "\r\n\r\n" + content;
                output.write(response.getBytes());
            } else if (HttpRequest[1].equals("/user-agent")) {
                boolean uaFound = false;
                while (!uaFound) {
                    String nextLine = reader.readLine();
                    if (nextLine.toLowerCase().startsWith("user-agent")) {
                        uaFound = true;
                        line = nextLine;
                    }
                }

                line = line.toLowerCase().replace("user-agent: ", "");
                String resp = Main.prepareHeaders(line.length()) + line;
//                System.out.println(resp);
                output.write(resp.getBytes());

            } else {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}