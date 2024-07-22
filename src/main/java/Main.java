import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class Main {
  public static void main(String[] args) {
    try {
      ServerSocket serverSocket = new ServerSocket(4221);

      // since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
      System.out.println("accepted new connection");
      InputStream input = clientSocket.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      String line = reader.readLine();
//      System.out.println(line);
      String[] HttpRequest = line.split(" ", 0);
      OutputStream output = clientSocket.getOutputStream();
      if (HttpRequest[1].equals("/")) {
        output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
      } else if (HttpRequest[1].startsWith("/echo")) {
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
        String resp = prepareHeaders(line.length()) + line;
        System.out.println(resp);
        output.write(resp.getBytes());

      } else {
        output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static String prepareHeaders(int contentLength) {
    return "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + contentLength + "\r\n\r\n";
  }
}
