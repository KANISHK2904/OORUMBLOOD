package util;

import java.util.*;

public class JsonUtil {

    public static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escape((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            return sb.append("]").toString();
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"" + escape(e.getKey().toString()) + "\":");
                sb.append(toJson(e.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    private static String escape(String s) {
        return s == null ? "" : s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /** Parse a flat JSON object {"key":"value",...} into a Map<String,String> */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        // Simple token-based parser
        int i = 0;
        while (i < json.length()) {
            // skip whitespace
            while (i < json.length() && (json.charAt(i) == ' ' || json.charAt(i) == ',' || json.charAt(i) == '\n' || json.charAt(i) == '\r')) i++;
            if (i >= json.length()) break;
            // parse key
            if (json.charAt(i) != '"') { i++; continue; }
            i++;
            int keyStart = i;
            while (i < json.length() && json.charAt(i) != '"') { if (json.charAt(i) == '\\') i++; i++; }
            String key = json.substring(keyStart, i);
            i++; // closing quote
            // skip colon
            while (i < json.length() && json.charAt(i) != ':') i++;
            i++;
            // skip whitespace
            while (i < json.length() && json.charAt(i) == ' ') i++;
            // parse value
            String value;
            if (i < json.length() && json.charAt(i) == '"') {
                i++;
                int valStart = i;
                while (i < json.length() && json.charAt(i) != '"') { if (json.charAt(i) == '\\') i++; i++; }
                value = json.substring(valStart, i).replace("\\\"", "\"").replace("\\\\", "\\");
                i++;
            } else {
                int valStart = i;
                while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                value = json.substring(valStart, i).trim();
            }
            map.put(key, value);
        }
        return map;
    }

    public static Map<String, Object> success(Object data) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("data", data);
        return m;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("error", message);
        return m;
    }
}
