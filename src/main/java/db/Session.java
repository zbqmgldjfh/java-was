package db;

import model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
    public static final String SESSION_ID = "sessionId";
    private static Map<String, Object> sessionDB = new ConcurrentHashMap<>();

    public static String save(Object value) {
        String sessionId = UUID.randomUUID().toString();
        sessionDB.put(sessionId, value);
        return sessionId;
    }

    public static void remove(String key) {
        sessionDB.remove(key);
    }

    public static Object getAttribute(String id) {
        return sessionDB.get(id);
    }
}
