import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

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

            } else if (HttpRequest[1].startsWith("/files/")) {
                String filename = HttpRequest[1].replace("/files/", "");
                File file = new File(Main.directory + filename);

                if (!file.exists())
                    output.write(Main.prepare404Headers().getBytes());
                else {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] fileContent = fileInputStream.readAllBytes();
                    byte[] headers = Main.prepareFileHeaders((int) file.length()).getBytes();

                    ByteBuffer byteBuffer = ByteBuffer.allocate(fileContent.length + headers.length);
                    byteBuffer.put(headers);
                    byteBuffer.put(fileContent);

                    byte[] response = byteBuffer.array();

                    output.write(response);
                }
            } else {
                output.write(Main.prepare404Headers().getBytes());
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}