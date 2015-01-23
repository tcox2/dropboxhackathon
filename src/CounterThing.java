import java.util.LinkedHashMap;
import java.util.Map;

public class CounterThing {

    private Map<String, Long> counters = new LinkedHashMap<String, Long>();

    public void inc(String key, Long value) {
        if (!counters.containsKey(key)) {
            counters.put(key, 0L);
        }
        long x = counters.get(key);
        x+=value;
        counters.put(key, x);
    }

    Map<String, Long> get() {
        return counters;
    }

}
