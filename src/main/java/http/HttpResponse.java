package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private static final Map<String, String> EXTENSIONS = new HashMap<>();
    Map<String, String> headers;
    private DataOutputStream dos;

    static {
        EXTENSIONS.put(".css", "text/css");
        EXTENSIONS.put(".js", "application/javascript");
        EXTENSIONS.put(".html", "text/html");
    }

    public HttpResponse(OutputStream out) {
        this.headers = new HashMap<>();
        this.dos = new DataOutputStream(out);
    }

    public void forward(String url) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            String extension = url.substring(url.lastIndexOf("."));
            response200Header(body.length, EXTENSIONS.get(extension));
            responseBody(body);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void forward(byte[] body) {
        try {
            response200Header(body.length, "text/html");
            responseBody(body);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void redirection(String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            setHeaders();
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void setHeaders() throws IOException {
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
    }

    private void response200Header(int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type:" + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }
}
