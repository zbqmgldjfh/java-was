package servlet;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.annotation.MyServletMapping;
import util.HttpRequestUtils;

import java.util.Map;

@MyServletMapping(url = "/user/create")
public class CreateUserServlet extends BaseServlet {
    private static final Logger log = LoggerFactory.getLogger(CreateUserServlet.class);

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        String resourcePath = request.getPath();
        response.forward(resourcePath);
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        String resourcePath = request.getPath();
        log.debug("[resourcePath] : {}", resourcePath);

        if (resourcePath.startsWith("/user/create")) {
            User user = createUser(request.getBody());
            String nextPath = "/";

            try {
                DataBase.addUser(user);
                log.debug("[User] : {}", user);
            } catch (RuntimeException e) {
                nextPath = "/user/form.html";
            }

            response.redirection(nextPath);
            return;
        }

        response.forward(resourcePath);
    }

    private User createUser(String resourcePath) {
        String queryString = HttpRequestUtils.getQueryString(resourcePath);
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        return new User(parameters.get("userId"), parameters.get("password"), parameters.get("name"), parameters.get("email"));
    }
}
