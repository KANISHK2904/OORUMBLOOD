import com.sun.net.httpserver.HttpServer;
import db.DatabaseConnection;
import server.*;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        // Initialize database
        System.out.println("[BBMS] Initializing Blood Bank Management System...");
        DatabaseConnection.createDatabaseAndTables();

        // Start HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // API routes
        server.createContext("/api/dashboard", new DashboardHandler());
        server.createContext("/api/donors", new DonorHandler());
        server.createContext("/api/patients", new PatientHandler());
        server.createContext("/api/hospitals", new HospitalHandler());
        server.createContext("/api/stock", new BloodStockHandler());
        server.createContext("/api/donations", new DonationHandler());
        server.createContext("/api/requests", new BloodRequestHandler());

        // Static file server (serve web/ directory)
        String webRoot = System.getProperty("user.dir") + java.io.File.separator + "web";
        server.createContext("/", new StaticFileHandler(webRoot));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("[BBMS] Server started at http://localhost:8080");
        System.out.println("[BBMS] Open your browser and navigate to http://localhost:8080");
    }
}
