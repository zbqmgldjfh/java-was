package servlet;

import db.DataBase;
import db.Session;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.annotation.MyServletMapping;
import util.HttpRequestUtils;
import webserver.RequestHandler;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@MyServletMapping(url = "/user/login")
public class LoginServlet extends BaseServlet {
    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.forward("/user/login.html");
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        Map<String, String> parsedMap = parseRequestBody(request);

        String userId = parsedMap.get("userId");
        String password = parsedMap.get("password");
        Optional<User> findUser = DataBase.findUserById(userId);
        log.debug("[User Status] : {}, {}", userId, password);

        if (isValidUser(password, findUser)) {
            createCookie(response, findUser);
            response.redirection("/");
            return;
        }

        response.redirection("/user/login_failed.html");
    }

    private void createCookie(HttpResponse response, Optional<User> findUser) {
        String sessionId = Session.save(findUser.get());
        response.addHeader("Set-Cookie", Session.SESSION_ID + "=" + sessionId + "; Path=/");
        response.addHeader("Cache-Control", "Max-Age=" + 1800);
    }

    private boolean isValidUser(String password, Optional<User> findUser) {
        return findUser.isPresent() && findUser.get().isSamePassword(password);
    }

    private Map<String, String> parseRequestBody(HttpRequest request) {
        String body = request.getBody();
        String queryString = HttpRequestUtils.getQueryString(body);
        return HttpRequestUtils.parseQueryString(queryString);
    }
}
