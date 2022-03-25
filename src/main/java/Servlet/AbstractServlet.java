package Servlet;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

public abstract class AbstractServlet implements Servlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        if (method.isPost()) {
            doPost(request, response);
        } else {
            doGet(request, response);
        }
    }

    public void doPost(HttpRequest request, HttpResponse response) {
    }

    public void doGet(HttpRequest request, HttpResponse response) {
    }
}
