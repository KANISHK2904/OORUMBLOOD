package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.HospitalDao;
import model.Hospital;
import util.JsonUtil;

import java.io.IOException;
import java.util.*;

/**
 * HospitalHandler — handles /api/hospitals and /api/hospitals/search
 */
public class HospitalHandler extends BaseHandler implements HttpHandler {
    private final HospitalDao dao = new HospitalDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();       // e.g. /api/hospitals/search
        String query  = exchange.getRequestURI().getRawQuery();   // e.g. q=apollo

        try {
            if ("GET".equals(method)) {
                // ---- Search endpoint: GET /api/hospitals/search?q=...
                if (path.endsWith("/search")) {
                    String q = extractQueryParam(query, "q");
                    if (q == null || q.isBlank()) {
                        sendOk(exchange, new ArrayList<>());
                        return;
                    }
                    List<Hospital> results = dao.search(q);
                    sendOk(exchange, hospitalsToSearchList(results));
                    return;
                }
                // ---- List all: GET /api/hospitals
                sendOk(exchange, hospitalsToList(dao.findAll()));

            } else if ("POST".equals(method)) {
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validate(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                dao.create(fromMap(body));
                sendOk(exchange, "Hospital registered successfully.");

            } else if ("PUT".equals(method)) {
                int id = extractId(exchange);
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validate(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                Hospital h = fromMap(body);
                h.setId(id);
                dao.update(h);
                sendOk(exchange, "Hospital updated successfully.");

            } else if ("DELETE".equals(method)) {
                int id = extractId(exchange);
                dao.delete(id);
                sendOk(exchange, "Hospital deleted successfully.");

            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry")) {
                sendError(exchange, 409, "A hospital with this phone or registration number already exists.");
            } else {
                sendError(exchange, 500, "Server error: " + msg);
            }
        }
    }

    // ------------------------------------------------------------------ helpers
    private String validate(Map<String, String> b) {
        if (isBlank(b.get("name")))  return "Hospital name is required.";
        if (isBlank(b.get("phone"))) return "Phone is required.";
        if (!b.get("phone").matches("[0-9+\\-\\s]{7,15}")) return "Invalid phone number.";
        return null;
    }

    private Hospital fromMap(Map<String, String> b) {
        Hospital h = new Hospital();
        h.setName(b.getOrDefault("name", ""));
        h.setPhone(b.getOrDefault("phone", ""));
        h.setEmail(b.getOrDefault("email", ""));
        h.setAddress(b.getOrDefault("address", ""));
        h.setRegistrationNumber(b.getOrDefault("registrationNumber", ""));
        return h;
    }

    /** Full list for Hospital Management page — unchanged shape */
    private List<Map<String, Object>> hospitalsToList(List<Hospital> hospitals) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Hospital h : hospitals) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",                 h.getId());
            m.put("name",               h.getName());
            m.put("phone",              h.getPhone());
            m.put("email",              h.getEmail());
            m.put("address",            h.getAddress());
            m.put("registrationNumber", h.getRegistrationNumber());
            list.add(m);
        }
        return list;
    }

    /** Compact shape for autocomplete search results */
    private List<Map<String, Object>> hospitalsToSearchList(List<Hospital> hospitals) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Hospital h : hospitals) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("hospitalId", h.getId());
            m.put("name",       h.getName());
            m.put("code",       h.getRegistrationNumber() != null ? h.getRegistrationNumber() : "");
            m.put("address",    h.getAddress() != null ? h.getAddress() : "");
            list.add(m);
        }
        return list;
    }

    /** Extracts ?key=value from a raw query string (URL-decoded). */
    private String extractQueryParam(String rawQuery, String key) {
        if (rawQuery == null) return null;
        for (String part : rawQuery.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                try { return java.net.URLDecoder.decode(kv[1], "UTF-8"); }
                catch (Exception e) { return kv[1]; }
            }
        }
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}
