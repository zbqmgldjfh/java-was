package webserver;

import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.*;
import servlet.filter.LoginFilter;
import util.RequestParser;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static Map<String, Servlet> servletMap = new ConcurrentHashMap<>();
    private static LoginFilter loginFilter = new LoginFilter();

    static {
        servletMap.put("/user/create", new CreateUserServlet());
        servletMap.put("/user/login", new LoginServlet());
        servletMap.put("/user/logout", new LogoutServlet());
        servletMap.put("/user/list", new UserListServlet());
        servletMap.put("/questions", new ArticleServlet());
        servletMap.put("/", new HomeServlet());
        loginFilter.addUrl("/user/list");
    }

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = RequestParser.parse(in);
            HttpResponse response = new HttpResponse(out);

            // 여기사 로그인 검증
            if (loginFilter.doFilter(request, response)) {
                controlServlet(request, response);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void controlServlet(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();
        log.debug("[PATH] : {}", path);
        if (servletMap.containsKey(path)) {
            Servlet servlet = servletMap.get(path);
            servlet.service(request, response);
            return;
        }
        response.forward(path);
    }
}
