package http;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private OutputStream out;
    private String statusLine;
    private Map<String, String> header;
    private byte[] body;

    public HttpResponse(OutputStream out) {
        this.out = out;
    }

    public HttpResponse(Builder builder) {
        this.statusLine = builder.statusLine;
        this.header = builder.header;
        this.body = builder.body;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }

    public static class Builder {

        private String statusLine;
        private Map<String, String> header = new HashMap<>();
        private byte[] body;

        public Builder status(String status) {
            this.statusLine = status;
            return this;
        }

        public Builder setHeader(String key, String value) {
            this.header.put(key, value);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusLine='" + statusLine + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}