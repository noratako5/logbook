package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import logbook.gui.ApplicationMain;
import logbook.util.JacksonUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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

    public  static Boolean needConvert(){
        List<Path> fileListOld = AkakariSyutsugekiLogRecorder.allFilePathOld();
        List<Path> fileList = AkakariSyutsugekiLogRecorder.allFilePath();
        if(fileListOld == null){
            return false;
        }
        if(fileList == null){
            return true;
        }
        for(Path oldPath:fileListOld){
            String name = oldPath.getFileName().toString();
            if(name.contains("error")){
                continue;
            }
            String woext = name.substring(0,name.lastIndexOf('.'));
            Boolean exist = false;
            for(Path path:fileList){
                if(path.getFileName().toString().contains(woext)){
                    exist = true;
                    break;
                }
            }
            if(!exist){
                AkakariSyutsugekiLog[] logArray = AkakariMapper.readSyutsugekiLogFromMessageZstdFile(oldPath.toFile());
                if(logArray != null){
                    return true;
                }
            }
        }
        return false;
    }
    public static void convertAllOldLog(){
        List<Path> fileListOld = AkakariSyutsugekiLogRecorder.allFilePathOld();
        List<Path> fileList = AkakariSyutsugekiLogRecorder.allFilePath();
        if(fileListOld == null){
            return;
        }
        for(Path oldPath:fileListOld){
            String name = oldPath.getFileName().toString();
            if(name.contains("error")){
                continue;
            }
            String woext = name.substring(0,name.lastIndexOf('.'));
            Boolean exist = false;
            if(fileList != null) {
                for (Path path : fileList) {
                    if (path.getFileName().toString().contains(woext)) {
                        exist = true;
                        break;
                    }
                }
            }
            if(!exist){
                convertLog(oldPath);
            }
        }
    }
    static void convertLog(Path oldPath){
        File dir = new File(AkakariSyutsugekiLogRecorder.syutsugekiLogPath);
        if(!dir.exists()){
            if(!dir.mkdirs()){
                //作成失敗
                return;
            }
        }
        AkakariSyutsugekiLog[] logArray = AkakariMapper.readSyutsugekiLogFromMessageZstdFile(oldPath.toFile());
        if(logArray == null){
            return;
        }
        ApplicationMain.logPrint(oldPath.getFileName().toString());
        ArrayList<AkakariSyutsugekiLog> list = new ArrayList<>();
        Path path = null;
        for(AkakariSyutsugekiLog log : logArray){
            Path path2 = AkakariSyutsugekiLogRecorder.dateToPath(log.start_port.date);
            {
                File dir2 = AkakariSyutsugekiLogRecorder.dateToDirPath(log.start_port.date).toFile();
                if(!dir2.exists()){
                    if(!dir2.mkdirs()){
                        return;
                    }
                }
            }
            if(path == null){
                path = path2;
            }
            if(!path.equals(path2)){
                AkakariSyutsugekiLog[] result = list.toArray(new AkakariSyutsugekiLog[0]);
                AkakariMapper.writeObjectToMessageZstdFile(result,path.toFile());
                list.clear();
                path = path2;
            }
            list.add(log);
        }
        if(path != null) {
            AkakariSyutsugekiLog[] result = list.toArray(new AkakariSyutsugekiLog[0]);
            AkakariMapper.writeObjectToMessageZstdFile(result, path.toFile());
            list.clear();
        }
    }

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
        calendar.add(Calendar.HOUR,1);
        Date nextDate = calendar.getTime();
        {
            //時間境界またぐケース考えて次の時間まで探索。それ以上は追わないので1時間以上かけた出撃は探索失敗する
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
