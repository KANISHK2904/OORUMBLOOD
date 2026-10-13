package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.HospitalDao;
import dao.PatientDao;
import model.Hospital;
import model.Patient;
import util.JsonUtil;

import java.io.IOException;
import java.util.*;

/**
 * PatientHandler — handles /api/patients
 * Now accepts and validates hospitalId on create/update.
 */
public class PatientHandler extends BaseHandler implements HttpHandler {
    private final PatientDao  patientDao  = new PatientDao();
    private final HospitalDao hospitalDao = new HospitalDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        String method = exchange.getRequestMethod();
        try {
            if ("GET".equals(method)) {
                sendOk(exchange, patientsToList(patientDao.findAll()));

            } else if ("POST".equals(method)) {
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validate(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                // Server-side hospital existence check
                String hospErr = validateHospital(body);
                if (hospErr != null) { sendError(exchange, 400, hospErr); return; }
                patientDao.create(fromMap(body));
                sendOk(exchange, "Patient created successfully.");

            } else if ("PUT".equals(method)) {
                int id = extractId(exchange);
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validate(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                String hospErr = validateHospital(body);
                if (hospErr != null) { sendError(exchange, 400, hospErr); return; }
                Patient p = fromMap(body);
                p.setId(id);
                patientDao.update(p);
                sendOk(exchange, "Patient updated successfully.");

            } else if ("DELETE".equals(method)) {
                int id = extractId(exchange);
                patientDao.delete(id);
                sendOk(exchange, "Patient deleted successfully.");

            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry")) {
                sendError(exchange, 409, "A patient with this phone number already exists.");
            } else {
                sendError(exchange, 500, "Server error: " + msg);
            }
        }
    }

    // ------------------------------------------------------------------ validation
    private String validate(Map<String, String> b) {
        if (isBlank(b.get("name")))  return "Name is required.";
        if (isBlank(b.get("phone"))) return "Phone is required.";
        if (!b.get("phone").matches("[0-9+\\-\\s]{7,15}")) return "Invalid phone number.";
        if (isBlank(b.get("age")))   return "Age is required.";
        try { if (Integer.parseInt(b.get("age")) <= 0) return "Age must be positive."; }
        catch (NumberFormatException e) { return "Age must be a number."; }
        if (isBlank(b.get("bloodGroup"))) return "Blood group is required.";
        if (!Arrays.asList("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(b.get("bloodGroup")))
            return "Invalid blood group.";
        // hospitalId is optional — validated for existence only when a non-zero value is provided
        return null;
    }

    /**
     * Verifies the provided hospitalId actually exists in the hospital table.
     * Returns a friendly error string, or null if OK.
     */
    private String validateHospital(Map<String, String> body) throws Exception {
        String hIdStr = body.get("hospitalId");
        if (isBlank(hIdStr) || "0".equals(hIdStr.trim())) return null; // caught in validate()
        int hId;
        try { hId = Integer.parseInt(hIdStr.trim()); }
        catch (NumberFormatException e) { return "Invalid hospital ID."; }
        Hospital h = hospitalDao.findById(hId);
        if (h == null) return "Selected hospital does not exist. Please pick a valid hospital.";
        return null;
    }

    // ------------------------------------------------------------------ mapping
    private Patient fromMap(Map<String, String> b) {
        Patient p = new Patient();
        p.setName(b.getOrDefault("name", ""));
        p.setPhone(b.getOrDefault("phone", ""));
        p.setAge(Integer.parseInt(b.getOrDefault("age", "0")));
        p.setBloodGroup(b.getOrDefault("bloodGroup", ""));
        p.setAddress(b.getOrDefault("address", ""));
        p.setDisease(b.getOrDefault("disease", ""));
        String hId = b.getOrDefault("hospitalId", "0").trim();
        try { p.setHospitalId(Integer.parseInt(hId)); }
        catch (NumberFormatException e) { p.setHospitalId(0); }
        return p;
    }

    private List<Map<String, Object>> patientsToList(List<Patient> patients) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Patient p : patients) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           p.getId());
            m.put("name",         p.getName());
            m.put("phone",        p.getPhone());
            m.put("age",          p.getAge());
            m.put("bloodGroup",   p.getBloodGroup());
            m.put("address",      p.getAddress());
            m.put("disease",      p.getDisease());
            m.put("hospitalId",   p.getHospitalId());
            m.put("hospitalName", p.getHospitalName());
            m.put("hospitalCode", p.getHospitalCode());
            list.add(m);
        }
        return list;
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}
