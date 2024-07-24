public class Headers {
    private String statusLine = "HTTP/1.1 200 OK";

    private final StringBuilder body = new StringBuilder();

    public Headers() {

    }

    public Headers(String statusLine) {
        this.statusLine = statusLine;
    }

    public Headers setStatusLine(String statusLine) {
        this.statusLine = statusLine;
        return this;
    }

    public void appendBody (String line) {
        body.append(line).append("\r\n");
    }

    public String getHeaders() {
        return statusLine + "\r\n" + body + "\r\n";
    }
}
