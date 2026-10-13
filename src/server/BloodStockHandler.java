package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.BloodStockDao;
import model.BloodStock;
import util.JsonUtil;

import java.io.IOException;
import java.util.*;

public class BloodStockHandler extends BaseHandler implements HttpHandler {
    private final BloodStockDao dao = new BloodStockDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<BloodStock> stocks = dao.findAll();
                List<Map<String, Object>> list = new ArrayList<>();
                for (BloodStock s : stocks) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("bloodGroup", s.getBloodGroup());
                    m.put("quantity", s.getQuantity());
                    list.add(m);
                }
                sendOk(exchange, list);
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }
}
