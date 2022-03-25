package Servlet;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import util.HttpRequestUtils;

import java.util.Map;

public class CreateUserServlet extends AbstractServlet {

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        String nextPath = "/index.html";
        try {
            User user = createUser(request.getBody());
            DataBase.addUser(user);
        } catch (RuntimeException e) {
            nextPath = "/user/form.html"; // 중복 회원일 경우 가입 Form으로 이동
        }
        response.sendRedirect(nextPath);
    }

    private User createUser(String resourcePath) {
        String queryString = HttpRequestUtils.getQueryString(resourcePath);
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        return new User(parameters.get("userId"), parameters.get("password"), parameters.get("name"), parameters.get("email"));
    }
}
