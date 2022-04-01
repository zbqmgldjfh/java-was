package servlet.map;

import servlet.Servlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServletMap {
    private static Map<String, Servlet> servletMap = new ConcurrentHashMap<>();

    public static void addServlet(String url, Servlet servlet) {
        servletMap.put(url, servlet);
    }

    public static boolean containsKey(String path) {
        return servletMap.containsKey(path);
    }

    public static Servlet get(String path) {
        return servletMap.get(path);
    }
}
