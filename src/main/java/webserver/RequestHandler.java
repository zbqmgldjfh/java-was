package webserver;

import Servlet.CreateUserServlet;
import Servlet.*;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestParser;

import java.io.*;
import java.net.Socket;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    public static final String HOME = "/index.html";
    public static final String USER_REGISTRY_FORM = "/user/form.html";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = buildRequest(in);
            HttpResponse response = buildResponse(out);
            controlServlet(request, response);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void controlServlet(HttpRequest request, HttpResponse response) throws IOException {
        String resourcePath = request.getPath();

        if (resourcePath.startsWith("/user/create")) {
            Servlet servlet = new CreateUserServlet();
            servlet.service(request, response);
            return;
        }

        Servlet servlet = new HomeServlet();
        servlet.service(request, response);
    }

    private HttpRequest buildRequest(InputStream in) throws IOException {
        return new RequestParser(in).createRequest();
    }

    private HttpResponse buildResponse(OutputStream out) {
        return new HttpResponse(out);
    }
}
