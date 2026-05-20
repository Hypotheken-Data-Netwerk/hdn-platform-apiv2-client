package nl.hdn.api;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigUtils {
    public static final String SKIP_ONBEHALFOF_VALIDATION = "SKIP_ONBEHALFOF_VALIDATION";
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private ConfigUtils() {}

    public static void clearCache() {
        CACHE.clear();
    }

    public static String get(String key) {
        return CACHE.computeIfAbsent(key, k -> {
            String env = System.getenv(k);
            return (env != null) ? env : System.getProperty(k);
        });
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        return (v == null) ? defaultValue : v.equalsIgnoreCase("true");
    }
}
