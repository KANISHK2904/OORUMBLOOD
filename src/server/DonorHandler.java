package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.DonorDao;
import model.Donor;
import util.JsonUtil;

import java.io.IOException;
import java.util.*;

public class DonorHandler extends BaseHandler implements HttpHandler {
    private final DonorDao dao = new DonorDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        try {
            if ("GET".equals(method)) {
                if (query != null && query.startsWith("search=")) {
                    String q = java.net.URLDecoder.decode(query.substring(7), "UTF-8");
                    List<Donor> donors = dao.search(q);
                    sendOk(exchange, donorsToList(donors));
                } else {
                    sendOk(exchange, donorsToList(dao.findAll()));
                }
            } else if ("POST".equals(method)) {
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validateDonor(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                Donor d = fromMap(body);
                dao.create(d);
                sendOk(exchange, "Donor created successfully.");
            } else if ("PUT".equals(method)) {
                int id = extractId(exchange);
                if (id < 0) { sendError(exchange, 400, "Invalid ID"); return; }
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validateDonor(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                Donor d = fromMap(body);
                d.setId(id);
                dao.update(d);
                sendOk(exchange, "Donor updated successfully.");
            } else if ("DELETE".equals(method)) {
                int id = extractId(exchange);
                if (id < 0) { sendError(exchange, 400, "Invalid ID"); return; }
                dao.delete(id);
                sendOk(exchange, "Donor deleted successfully.");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry") && msg.contains("phone")) {
                sendError(exchange, 409, "A donor with this phone number already exists.");
            } else {
                sendError(exchange, 500, "Server error: " + (msg != null ? msg : "Unknown"));
            }
        }
    }

    private String validateDonor(Map<String, String> b) {
        if (isBlank(b.get("name"))) return "Name is required.";
        if (isBlank(b.get("phone"))) return "Phone is required.";
        if (!b.get("phone").matches("[0-9+\\-\\s]{7,15}")) return "Invalid phone number format.";
        if (isBlank(b.get("age"))) return "Age is required.";
        try {
            int age = Integer.parseInt(b.get("age"));
            if (age < 18 || age > 65) return "Donor age must be between 18 and 65.";
        } catch (NumberFormatException e) { return "Age must be a number."; }
        if (isBlank(b.get("bloodGroup"))) return "Blood group is required.";
        if (!isValidBloodGroup(b.get("bloodGroup"))) return "Invalid blood group.";
        return null;
    }

    private Donor fromMap(Map<String, String> b) {
        Donor d = new Donor();
        d.setName(b.getOrDefault("name", ""));
        d.setPhone(b.getOrDefault("phone", ""));
        d.setEmail(b.getOrDefault("email", ""));
        d.setAge(Integer.parseInt(b.getOrDefault("age", "0")));
        d.setBloodGroup(b.getOrDefault("bloodGroup", ""));
        d.setAddress(b.getOrDefault("address", ""));
        d.setLastDonationDate(b.getOrDefault("lastDonationDate", ""));
        return d;
    }

    private List<Map<String, Object>> donorsToList(List<Donor> donors) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Donor d : donors) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("name", d.getName());
            m.put("phone", d.getPhone());
            m.put("email", d.getEmail());
            m.put("age", d.getAge());
            m.put("bloodGroup", d.getBloodGroup());
            m.put("address", d.getAddress());
            m.put("lastDonationDate", d.getLastDonationDate());
            list.add(m);
        }
        return list;
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
    private boolean isValidBloodGroup(String g) {
        return Arrays.asList("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(g);
    }
}
