package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import logbook.dto.ItemInfoDto;
import org.jetbrains.annotations.Nullable;

import javax.json.Json;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by noratako5 on 2017/10/28.
 */
public class AkakariMasterLogReader {
    private static Map<Date,JsonNode> masterDateToMasterJsonCache = Collections.synchronizedMap(new AkakariCacheMap<>(4));
    private static Map<Date,Map<String,JsonNode>> masterDateToShipMasterJsonCache = Collections.synchronizedMap(new AkakariCacheMap<>(4));
    private static Map<Date,Map<String,ItemInfoDto>> masterDateToItemInfoCache = Collections.synchronizedMap(new AkakariCacheMap<>(4));
    private static List<Date> masterDateList;
    public static void updateMasterDateList(AkakariMasterLog[] logArray){
        masterDateList = new ArrayList<>();
        for(AkakariMasterLog log : logArray){
            masterDateList.add(log.date);
        }
        Collections.sort(masterDateList);
        Collections.reverse(masterDateList);
    }
    @Nullable
    public static Date dateToMasterDate(Date date){
        if(masterDateList == null){
            Path path = AkakariMasterLogRecorder.getPath();
            AkakariMasterLog[] logArray = AkakariMapper.readMasterLogFromMessageZstdFile(path.toFile());
            updateMasterDateList(logArray);
        }
        for(Date masterDate : masterDateList){
            if(date.after(masterDate)){
                return masterDate;
            }
        }
        return null;
    }

    @Nullable
    public static  JsonNode dateToMaster(Date date){
        Date masterDate = dateToMasterDate(date);
        if(masterDate == null){
            return null;
        }
        if(masterDateToMasterJsonCache.containsKey(masterDate)){
            return masterDateToMasterJsonCache.get(masterDate);
        }

        Path path = AkakariMasterLogRecorder.getPath();
        AkakariMasterLog[] logArray = AkakariMapper.readMasterLogFromMessageZstdFile(path.toFile());
        for(AkakariMasterLog log : logArray){
            if(log.date.equals(masterDate)){
                masterDateToMasterJsonCache.put(masterDate,log.getBody());
                return masterDateToMasterJsonCache.get(masterDate);
            }
        }
        return null;
    }
    @Nullable
    public static Map<String,JsonNode> dateToShipMasterJson(Date date){
        Date masterDate = dateToMasterDate(date);
        if(masterDate == null){
            return null;
        }
        if(masterDateToShipMasterJsonCache.containsKey(masterDate)){
            return masterDateToShipMasterJsonCache.get(masterDate);
        }
        ArrayNode shipMaster;
        {
            JsonNode master = dateToMaster(date);
            if (!(master instanceof ObjectNode)) {
                return null;
            }
            ObjectNode masterObject = (ObjectNode)master;
            JsonNode ship = masterObject.get("api_mst_ship");
            if(!(ship instanceof  ArrayNode)){
                return null;
            }
            shipMaster = (ArrayNode)ship;
        }
        Map<String,JsonNode> map = new HashMap<>();
        for(JsonNode ship : shipMaster){
            if(!(ship instanceof ObjectNode)){
                continue;
            }
            map.put(ship.get("api_id").asText(),ship);
        }
        masterDateToShipMasterJsonCache.put(masterDate,map);
        return map;
    }
    @Nullable
    public static Map<String,ItemInfoDto> dateToItemInfoDto(Date date){
        Date masterDate = dateToMasterDate(date);
        if(masterDate == null){
            return null;
        }
        if(masterDateToItemInfoCache.containsKey(masterDate)){
            return masterDateToItemInfoCache.get(masterDate);
        }
        ArrayNode itemMaster;
        {
            JsonNode master = dateToMaster(date);
            if (!(master instanceof ObjectNode)) {
                return null;
            }
            ObjectNode masterObject = (ObjectNode)master;
            JsonNode item = masterObject.get("api_mst_slotitem");
            if(!(item instanceof  ArrayNode)){
                return null;
            }
            itemMaster = (ArrayNode)item;
        }
        Map<String,ItemInfoDto> map = new HashMap<>();
        for(JsonNode item : itemMaster){
            if(!(item instanceof ObjectNode)){
                continue;
            }
            map.put(item.get("api_id").asText(),new ItemInfoDto(Json.createReader(new StringReader(item.toString())).readObject()));
        }
        masterDateToItemInfoCache.put(masterDate,map);
        return map;
    }
    @Nullable
    public static JsonNode dateAndIdToShipMasterJson(Date date, String id){
        Map<String,JsonNode> map = dateToShipMasterJson(date);
        if(map == null){
            return null;
        }
        return  map.get(id);
    }
    @Nullable
    public static ItemInfoDto dateAndIdToItemMasterJson(Date date, String id){
        Map<String,ItemInfoDto> map = dateToItemInfoDto(date);
        if(map == null){
            return null;
        }
        return  map.get(id);
    }
}
