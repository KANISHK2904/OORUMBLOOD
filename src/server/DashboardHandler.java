package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.*;
import model.*;

import java.io.IOException;
import java.util.*;

public class DashboardHandler extends BaseHandler implements HttpHandler {
    private final DonorDao donorDao = new DonorDao();
    private final PatientDao patientDao = new PatientDao();
    private final HospitalDao hospitalDao = new HospitalDao();
    private final BloodStockDao stockDao = new BloodStockDao();
    private final BloodRequestDao requestDao = new BloodRequestDao();
    private final DonationDao donationDao = new DonationDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        try {
            Map<String, Object> data = new LinkedHashMap<>();

            // Totals
            data.put("totalDonors", donorDao.findAll().size());
            data.put("totalPatients", patientDao.findAll().size());
            data.put("totalHospitals", hospitalDao.findAll().size());

            List<BloodRequest> allReqs = requestDao.findAll();
            long pending = allReqs.stream().filter(r -> "Pending".equals(r.getStatus())).count();
            data.put("pendingRequests", pending);

            // Total available units
            List<BloodStock> stocks = stockDao.findAll();
            int totalUnits = stocks.stream().mapToInt(BloodStock::getQuantity).sum();
            data.put("totalUnits", totalUnits);

            // Stock by group
            List<Map<String, Object>> stockList = new ArrayList<>();
            for (BloodStock s : stocks) {
                Map<String, Object> sm = new LinkedHashMap<>();
                sm.put("bloodGroup", s.getBloodGroup());
                sm.put("quantity", s.getQuantity());
                stockList.add(sm);
            }
            data.put("bloodStock", stockList);

            // Recent donations (last 5)
            List<Donation> recentDonations = donationDao.findRecent(5);
            List<Map<String, Object>> donList = new ArrayList<>();
            for (Donation d : recentDonations) {
                Map<String, Object> dm = new LinkedHashMap<>();
                dm.put("id", d.getId());
                dm.put("donorName", d.getDonorName());
                dm.put("bloodGroup", d.getBloodGroup());
                dm.put("units", d.getUnits());
                dm.put("donationDate", d.getDonationDate());
                donList.add(dm);
            }
            data.put("recentDonations", donList);

            // Recent requests (last 5)
            List<BloodRequest> recentReqs = requestDao.findRecent(5);
            List<Map<String, Object>> reqList = new ArrayList<>();
            for (BloodRequest r : recentReqs) {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("id", r.getId());
                rm.put("patientName", r.getPatientName());
                rm.put("hospitalName", r.getHospitalName());
                rm.put("bloodGroup", r.getBloodGroup());
                rm.put("units", r.getUnits());
                rm.put("status", r.getStatus());
                rm.put("requestDate", r.getRequestDate());
                reqList.add(rm);
            }
            data.put("recentRequests", reqList);

            sendOk(exchange, data);
        } catch (Exception e) {
            sendError(exchange, 500, "Dashboard error: " + e.getMessage());
        }
    }
}
