package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.DonationDao;
import model.Donation;
import util.JsonUtil;

import java.io.IOException;
import java.util.*;

public class DonationHandler extends BaseHandler implements HttpHandler {
    private final DonationDao dao = new DonationDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        String method = exchange.getRequestMethod();
        try {
            if ("GET".equals(method)) {
                sendOk(exchange, donationsToList(dao.findAll()));
            } else if ("POST".equals(method)) {
                Map<String, String> body = JsonUtil.parseObject(readBody(exchange));
                String err = validate(body);
                if (err != null) { sendError(exchange, 400, err); return; }
                Donation d = new Donation();
                d.setDonorId(Integer.parseInt(body.get("donorId")));
                d.setBloodGroup(body.get("bloodGroup"));
                d.setUnits(Integer.parseInt(body.get("units")));
                d.setDonationDate(body.get("donationDate"));
                dao.recordDonation(d);
                sendOk(exchange, "Donation recorded and stock updated.");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private String validate(Map<String, String> b) {
        if (isBlank(b.get("donorId"))) return "Donor is required.";
        if (isBlank(b.get("bloodGroup"))) return "Blood group is required.";
        if (!Arrays.asList("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(b.get("bloodGroup"))) return "Invalid blood group.";
        if (isBlank(b.get("units"))) return "Units is required.";
        try { if (Integer.parseInt(b.get("units")) <= 0) return "Units must be greater than 0."; }
        catch (NumberFormatException e) { return "Units must be a number."; }
        if (isBlank(b.get("donationDate"))) return "Donation date is required.";
        return null;
    }

    private List<Map<String, Object>> donationsToList(List<Donation> donations) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Donation d : donations) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("donorId", d.getDonorId());
            m.put("donorName", d.getDonorName());
            m.put("bloodGroup", d.getBloodGroup());
            m.put("units", d.getUnits());
            m.put("donationDate", d.getDonationDate());
            list.add(m);
        }
        return list;
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}
