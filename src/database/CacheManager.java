import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    private static final Map<String, Object> cache = new HashMap<>();

    public static void put(String key, Object value) {
        cache.put(key, value);
    }

    public static Object get(String key) {
        return cache.get(key);
    }

    public static boolean has(String key) {
        return cache.containsKey(key);
    }

    public static void invalidate(String key) {
        cache.remove(key);
    }

    public static void invalidateByPrefix(String prefix) {
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public static void clearAll() {
        cache.clear();
    }
}
