package Servlet;

import Servlet.Servlet;
import http.HttpRequest;
import http.HttpResponse;

public class HomeServlet extends AbstractServlet {
    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.forward(request.getPath());
    }
}
