package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.BloodRequestDao;
import model.BloodRequest;
import util.JsonUtil;

import java.io.IOException;
import java.util.*;

public class BloodRequestHandler extends BaseHandler implements HttpHandler {
    private final BloodRequestDao dao = new BloodRequestDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            if ("GET".equals(method)) {
                sendOk(exchange, requestsToList(dao.findAll()));
            } else if ("PUT".equals(method)) {
                if (path.endsWith("/fulfill")) {
                    // PUT /api/requests/{id}/fulfill
                    String[] parts = path.split("/");
                    int id = Integer.parseInt(parts[parts.length - 2]);
                    String err = dao.fulfill(id);
                    if (err != null) { sendError(exchange, 400, err); return; }
                    sendOk(exchange, "Request fulfilled and stock updated.");
                } else if (path.endsWith("/reject")) {
                    String[] parts = path.split("/");
                    int id = Integer.parseInt(parts[parts.length - 2]);
                    dao.reject(id);
                    sendOk(exchange, "Request rejected.");
                } else {
                    sendError(exchange, 400, "Unknown PUT action.");
                }
            } else if ("POST".equals(method)) {
                // Create new request
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validate(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                BloodRequest req = new BloodRequest();
                req.setPatientId(Integer.parseInt(body.get("patientId")));
                req.setHospitalId(Integer.parseInt(body.get("hospitalId")));
                req.setBloodGroup(body.get("bloodGroup"));
                req.setUnits(Integer.parseInt(body.get("units")));
                dao.create(req);
                sendOk(exchange, "Blood request created.");
            } else if ("DELETE".equals(method)) {
                int id = extractId(exchange);
                dao.delete(id);
                sendOk(exchange, "Request deleted.");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private String validate(Map<String, String> b) {
        if (isBlank(b.get("patientId"))) return "Patient is required.";
        if (isBlank(b.get("hospitalId"))) return "Hospital is required.";
        if (isBlank(b.get("bloodGroup"))) return "Blood group is required.";
        if (!Arrays.asList("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(b.get("bloodGroup"))) return "Invalid blood group.";
        if (isBlank(b.get("units"))) return "Units is required.";
        try { if (Integer.parseInt(b.get("units")) <= 0) return "Units must be greater than 0."; }
        catch (NumberFormatException e) { return "Units must be a number."; }
        return null;
    }

    private List<Map<String, Object>> requestsToList(List<BloodRequest> reqs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BloodRequest r : reqs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("patientId", r.getPatientId());
            m.put("patientName", r.getPatientName());
            m.put("hospitalId", r.getHospitalId());
            m.put("hospitalName", r.getHospitalName());
            m.put("bloodGroup", r.getBloodGroup());
            m.put("units", r.getUnits());
            m.put("status", r.getStatus());
            m.put("requestDate", r.getRequestDate());
            list.add(m);
        }
        return list;
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}
