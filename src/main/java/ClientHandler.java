import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            String line = reader.readLine();
            String[] HttpRequest = line.split(" ", 0);

            Map<String, String> headers = getHeaders();

            Headers resH = new Headers();

            if (headers.containsKey("accept-encoding") && headers.get("accept-encoding").contains("gzip"))
                resH.appendBody("Content-Encoding: gzip");

            String body = getBody();

            OutputStream output = clientSocket.getOutputStream();

            if (HttpRequest[1].equals("/")) {
                output.write(resH.getHeaders().getBytes());
            } else if (HttpRequest[1].startsWith("/echo/")) {
                String content = HttpRequest[1].replace("/echo/", "");
                String response = Main.prepareHeaders(resH, content.length()) + content;
                output.write(response.getBytes());
            } else if (HttpRequest[1].equals("/user-agent")) {
                String ua = (String) headers.get("user-agent");

                String resp = Main.prepareHeaders(resH, ua.length()) + ua;
                output.write(resp.getBytes());

            } else if (HttpRequest[1].startsWith("/files/")) {
                String filename = HttpRequest[1].replace("/files/", "");
                File file = new File(Main.directory + filename);

                if (HttpRequest[0].equals("POST")) {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(body);
                    fileWriter.close();

                    resH.setStatusLine("HTTP/1.1 201 Created");

                    output.write(resH.getHeaders().getBytes());
                } else if (!file.exists())
                    output.write(resH.setStatusLine("HTTP/1.1 404 Not Found").getHeaders().getBytes());
                else {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] fileContent = fileInputStream.readAllBytes();
                    fileInputStream.close();

                    resH.appendBody("Content-Type: application/octet-stream");
                    resH.appendBody("Content-Length: " + file.length());

                    byte[] _headers = resH.getHeaders().getBytes();

                    ByteBuffer byteBuffer = ByteBuffer.allocate(fileContent.length + _headers.length);
                    byteBuffer.put(_headers);
                    byteBuffer.put(fileContent);

                    byte[] response = byteBuffer.array();

                    output.write(response);
                }
            } else {
                output.write(resH.setStatusLine("HTTP/1.1 404 Not Found").getHeaders().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Map getHeaders() throws IOException {
        Map<String, String> map = new HashMap<>();

        while (reader.ready()) {
            String line = reader.readLine();

            if (line.isEmpty()) {
                break;
            }

            String[] headerPair = line.split(": ");

            map.put(headerPair[0].toLowerCase(), headerPair[1]);
        }
        return map;
    }

    public String getBody() throws IOException {
        // Read body
        StringBuilder bodyBuffer = new StringBuilder();

        while (reader.ready()) {
            bodyBuffer.append((char)reader.read());
        }

        return bodyBuffer.toString();
    }
}