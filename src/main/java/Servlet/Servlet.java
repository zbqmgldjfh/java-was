package Servlet;

import http.HttpRequest;
import http.HttpResponse;

public interface Servlet {
    void service(HttpRequest request, HttpResponse response);
}
