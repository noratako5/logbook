package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import logbook.util.JacksonUtil;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by noratako5 on 2017/10/03.
 */
public class AkakariSyutsugekiLogReader {
    private static List<Date> startPortDateList = Collections.synchronizedList(new ArrayList<>());
    private static Map<Date,Date> battleDateToStartPortDateCache = Collections.synchronizedMap(new HashMap<>());
    //private static Map<Date,ArrayNode> battleDateToShipArrayCache = Collections.synchronizedMap(new AkakariCacheMap<>(0));
    private static Map<Date,AkakariSyutsugekiLog> startPortDateToLogCache = Collections.synchronizedMap(new AkakariCacheMap<>(4));
    private static Map<Date,AkakariSyutsugekiLog> startPortDateToNextLogCache = Collections.synchronizedMap(new AkakariCacheMap<>(4));
    private static Map<Path,AkakariSyutsugekiLog[]> zstdFilePathToLogArrayCache = Collections.synchronizedMap(new AkakariCacheMap<>(1));

    public static void loadAllStartPortDate(){
        List<Path> fileList = AkakariSyutsugekiLogRecorder.allFilePath();
        if(fileList == null){
            return;
        }
        for(Path path : fileList){
            AkakariSyutsugekiLog[] logArray = AkakariMapper.readSyutsugekiLogFromMessageZstdFile(path.toFile());
            if(logArray == null){
                continue;
            }
            for(AkakariSyutsugekiLog log : logArray){
                loadStartPortDate(log);
            }
            //AkakariSyutsugekiLogRecorder.createJson(path.toFile());
        }
        Collections.sort(startPortDateList);
    }
    public static void loadStartPortDate(AkakariSyutsugekiLog log){
        Date startPortDate = log.start_port.date;
        if(startPortDate != null){
            startPortDateList.add(startPortDate);
        }
    }
    public static void updateLogFile(Path path,AkakariSyutsugekiLog[] logArray){
        zstdFilePathToLogArrayCache.clear();
        startPortDateToLogCache.clear();
        startPortDateToNextLogCache.clear();
//        battleDateToShipArrayCache.clear();
    }

    @Nullable
    private static AkakariSyutsugekiLog[] zstdFilePathToLogArray(Path path){
        if(zstdFilePathToLogArrayCache.containsKey(path)){
            return zstdFilePathToLogArrayCache.get(path);
        }
        zstdFilePathToLogArrayCache.clear();
        AkakariSyutsugekiLog[] logArray = AkakariMapper.readSyutsugekiLogFromMessageZstdFile(path.toFile());
        if(logArray == null) {
            return null;
        }
        zstdFilePathToLogArrayCache.put(path,logArray);
        return logArray;
    }

    @Nullable
    private  static AkakariSyutsugekiLog startPortDateToLog(Date startPortDate){
        if(startPortDate == null){
            return null;
        }
        if(startPortDateToLogCache.containsKey(startPortDate)){
            return startPortDateToLogCache.get(startPortDate);
        }
        Path path = AkakariSyutsugekiLogRecorder.dateToPath(startPortDate);
        AkakariSyutsugekiLog[] logArray = zstdFilePathToLogArray(path);
        if(logArray == null){
            return null;
        }
        for(AkakariSyutsugekiLog log : logArray){
            if(log.start_port.date.equals(startPortDate)){
                startPortDateToLogCache.put(startPortDate,log);
                return log;
            }
        }
        return null;
    }
    @Nullable
    private  static AkakariSyutsugekiLog startPortDateToNextLog(Date startPortDate){
        if(startPortDate == null){
            return null;
        }
        if(startPortDateToNextLogCache.containsKey(startPortDate)){
            return startPortDateToNextLogCache.get(startPortDate);
        }
        {
            Path path = AkakariSyutsugekiLogRecorder.dateToPath(startPortDate);
            AkakariSyutsugekiLog[] logArray = zstdFilePathToLogArray(path);
            if (logArray == null) {
                return null;
            }
            for (AkakariSyutsugekiLog log : logArray) {
                if (log.start_port.date.after(startPortDate)) {
                    startPortDateToNextLogCache.put(startPortDate, log);
                    return log;
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startPortDate);
        calendar.add(Calendar.DATE,1);
        Date nextDate = calendar.getTime();
        {
            //夜中に日付またぐケース考えて次の日まで探索。それ以上は追わない
            Path path = AkakariSyutsugekiLogRecorder.dateToPath(nextDate);
            AkakariSyutsugekiLog[] logArray = zstdFilePathToLogArray(path);
            if (logArray == null) {
                return null;
            }
            for (AkakariSyutsugekiLog log : logArray) {
                if (log.start_port.date.after(startPortDate)) {
                    startPortDateToNextLogCache.put(startPortDate, log);
                    return log;
                }
            }
        }
        return null;
    }
    @Nullable
    public static Date battleDateToStartPortDate(Date battleDate){
        if(battleDate == null){
            return null;
        }
        if(battleDateToStartPortDateCache.containsKey(battleDate)){
            return battleDateToStartPortDateCache.get(battleDate);
        }
        Date portDate = null;
        for(Date date:startPortDateList){
            if(date.after(battleDate)){
                break;
            }
            portDate = date;
        }
        if(portDate == null){
            return null;
        }
        AkakariSyutsugekiLog log = startPortDateToLog(portDate);
        Date endPortDate = log.end_port.date;
        if(endPortDate.before(battleDate)){
            return null;
        }
        battleDateToStartPortDateCache.put(battleDate,portDate);
        return portDate;
    }
    @Nullable
    public static ArrayNode battleDateToShipArray(Date battleDate){
        if(battleDate == null){
            return null;
        }
//        if(battleDateToShipArrayCache.containsKey(battleDate)){
//            return battleDateToShipArrayCache.get(battleDate);
//        }
        Date startPortDate = battleDateToStartPortDate(battleDate);
        if(startPortDate == null){
            return null;
        }
        AkakariSyutsugekiLog log = startPortDateToLog(startPortDate);
        if(log == null){
            return null;
        }
        ArrayNode result = log.shipsAfterBattle(battleDate);
        if(result == null){
            return null;
        }
//        battleDateToShipArrayCache.put(battleDate,result);
        return result;
    }
    @Nullable
    public static AkakariSyutsugekiLog battleDateToLog(Date battleDate){
        return startPortDateToLog(battleDateToStartPortDate(battleDate));
    }
    @Nullable
    public static ObjectNode shipAfterBattle(Date battleDate, int shipId){
        ArrayNode shipArray = battleDateToShipArray(battleDate);
        if(shipArray == null){
            return null;
        }
        for(JsonNode node : shipArray){
            if(node instanceof  ObjectNode){
                int id = JacksonUtil.toInt(node.get("api_id"));
                if(id == shipId){
                    return (ObjectNode)node;
                }
            }
        }
        return null;
    }
    @Nullable
    public static ObjectNode shipEndPort(Date battleDate, int shipId){
        AkakariSyutsugekiLog log = battleDateToLog(battleDate);
        ArrayNode shipArray = log.end_port.ship;
        if(shipArray == null){
            return null;
        }
        for(JsonNode node : shipArray){
            if(node instanceof  ObjectNode){
                int id = JacksonUtil.toInt(node.get("api_id"));
                if(id == shipId){
                    return (ObjectNode)node;
                }
            }
        }
        return null;
    }

    @Nullable
    public static AkakariSyutsugekiAirBaseData battleDateToStartAirBaseData(Date battleDate){
        AkakariSyutsugekiLog log = startPortDateToLog(battleDateToStartPortDate(battleDate));
        if(log == null){
            return null;
        }
        return log.lastAirBase();
    }
    @Nullable
    public static AkakariSyutsugekiAirBaseData battleDateToEndAirBaseData(Date battleDate){
        AkakariSyutsugekiLog log = startPortDateToNextLog(battleDateToStartPortDate(battleDate));
        if(log == null){
            return null;
        }
        return log.firstAirBase();
    }
    public static int battleDateToAreaId(Date battleDate){
        AkakariSyutsugekiLog log = startPortDateToLog(battleDateToStartPortDate(battleDate));
        if(log == null){
            return -1;
        }
        return log.areaId();
    }
}
