package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class StaticFileHandler implements HttpHandler {
    private final Path webRoot;
    private static final Map<String, String> MIME = new HashMap<>();

    static {
        MIME.put("html", "text/html; charset=utf-8");
        MIME.put("css", "text/css; charset=utf-8");
        MIME.put("js", "application/javascript; charset=utf-8");
        MIME.put("json", "application/json");
        MIME.put("png", "image/png");
        MIME.put("jpg", "image/jpeg");
        MIME.put("jpeg", "image/jpeg");
        MIME.put("svg", "image/svg+xml");
        MIME.put("ico", "image/x-icon");
        MIME.put("woff2", "font/woff2");
        MIME.put("woff", "font/woff");
    }

    public StaticFileHandler(String webRoot) {
        this.webRoot = Paths.get(webRoot).toAbsolutePath();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uriPath = exchange.getRequestURI().getPath();
        if ("/".equals(uriPath) || uriPath.isEmpty()) uriPath = "/index.html";

        Path file = webRoot.resolve(uriPath.substring(1)).normalize();
        if (!file.startsWith(webRoot)) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        if (!Files.exists(file) || Files.isDirectory(file)) {
            // Fallback to index.html for SPA routing
            file = webRoot.resolve("index.html");
        }

        String ext = getExt(file.toString());
        String mime = MIME.getOrDefault(ext, "application/octet-stream");
        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getExt(String filename) {
        int i = filename.lastIndexOf('.');
        return i < 0 ? "" : filename.substring(i + 1).toLowerCase();
    }
}
