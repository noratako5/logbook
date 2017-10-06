package logbook.builtinscript.akakariLog;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by noratako5 on 2017/10/04.
 */
public class AkakariCacheMap<K,V> extends LinkedHashMap<K,V>{
    private int limit;
    public AkakariCacheMap(int limit){
        super(limit);
        this.limit = limit;
    }
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > this.limit;
    }
}
