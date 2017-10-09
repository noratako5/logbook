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
    private static List<Date> startPortDateList = new ArrayList<>();
    private static Map<Date,Date> battleDateToStartPortDateCache = new HashMap<>();
    private static AkakariCacheMap<Date,ArrayNode> battleDateToShipArrayCache = new AkakariCacheMap<>(16);
    private static AkakariCacheMap<Date,AkakariSyutsugekiLog> startPortDateToLogCache = new AkakariCacheMap<>(16);
    private static AkakariCacheMap<Path,AkakariSyutsugekiLog[]> zstdFilePathToLogArrayCache = new AkakariCacheMap<Path, AkakariSyutsugekiLog[]>(4);

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
        zstdFilePathToLogArrayCache.clear();
    }

    @Nullable
    private static AkakariSyutsugekiLog[] zstdFilePathToLogArray(Path path){
        if(zstdFilePathToLogArrayCache.containsKey(path)){
            return zstdFilePathToLogArrayCache.get(path);
        }
        AkakariSyutsugekiLog[] logArray = AkakariMapper.readSyutsugekiLogFromMessageZstdFile(path.toFile());
        if(logArray == null) {
            return null;
        }
        zstdFilePathToLogArrayCache.put(path,logArray);
        return logArray;
    }

    @Nullable
    private  static AkakariSyutsugekiLog startPortDateToLog(Date startPortDate){
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
    public static Date battleDateToStartPortDate(Date battleDate){
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
        if(battleDateToShipArrayCache.containsKey(battleDate)){
            return battleDateToShipArrayCache.get(battleDate);
        }
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
        battleDateToShipArrayCache.put(battleDate,result);
        return result;
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


}
