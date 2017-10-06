package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import logbook.data.AkakariData;
import logbook.data.Data;
import logbook.internal.LoggerHolder;
import logbook.util.JacksonUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by noratako5 on 2017/10/01.
 */
public class AkakariSyutsugekiData {
    private static LoggerHolder LOG = new LoggerHolder("builtinScript");

    public Date date;
    public String api_name;
    public JsonNode body;
    public ObjectNode req;
    public ArrayNode slot_item;


    private static List<Integer> readSlotId(JsonNode slotId){
        List<Integer> itemIds = new ArrayList<>();
        if(slotId instanceof ArrayNode){
            int[] ids = JacksonUtil.toIntArray(slotId);
            for(int id : ids){
                if(id > 0){
                    itemIds.add(id);
                }
            }
        }
        else{
            int id = JacksonUtil.toInt(slotId);
            if(id > 0){
                itemIds.add(id);
            }
        }
        return itemIds;
    }
    @Nullable
    private static ArrayNode readSlotItems(JsonNode body, List<AkakariData> startSlotItemArray){
        if(!(body instanceof  ObjectNode)){
            return null;
        }
        List<JsonNode> planeInfoList = new ArrayList<>();
        if(body.get("api_plane_info") != null){
            planeInfoList.add(body.get("api_plane_info"));
        }
        else{
            JsonNode airBase = body.get("api_air_base");
            if(airBase instanceof ArrayNode){
                for(JsonNode node : airBase) {
                    JsonNode info = node.get("api_plane_info");
                    if(info != null){
                        planeInfoList.add(info);
                    }
                }
            }
        }
        if(planeInfoList.isEmpty()){
            return null;
        }

        ///基地航空系APIで出現した装備ID
        List<Integer> itemIds = new ArrayList<>();
        for(JsonNode planeInfo : planeInfoList) {
            if (planeInfo instanceof ObjectNode) {
                itemIds.addAll(readSlotId(planeInfo.get("api_slotid")));
            } else if (planeInfo instanceof ArrayNode) {
                ArrayNode array = (ArrayNode) planeInfo;
                for (JsonNode node : array) {
                    itemIds.addAll(readSlotId(node.get("api_slotid")));
                }
            }
        }
        if(itemIds.isEmpty()){
            return null;
        }

        Map<Integer,ObjectNode> tmpMap = new HashMap<>();
        for (Data item : startSlotItemArray) {
            List<ObjectNode> itemList = new ArrayList<>();
            switch (item.getDataType()) {
                case SLOTITEM_MEMBER: {
                    ArrayNode itemJson = AkakariMapper.jsonToArrayNode(item.getJsonObject().get("api_data").toString());
                    if(itemJson == null){
                        LOG.get().warn("SLOTITEM_MEMBER itemJson == null");
                        continue;
                    }
                    for (JsonNode object : itemJson) {
                        if (object instanceof ObjectNode) {
                            itemList.add((ObjectNode) object);
                        }
                    }
                    break;
                }
                case REQUIRE_INFO: {
                    ObjectNode itemJson = AkakariMapper.jsonToObjectNode(item.getJsonObject().get("api_data").toString());
                    if(itemJson == null){
                        LOG.get().warn("REQUIRE_INFO itemJson == null");
                        continue;
                    }
                    JsonNode listObject = itemJson.get("api_slot_item");
                    if(listObject instanceof  ArrayNode) {
                        ArrayNode list = (ArrayNode)listObject;
                        for (JsonNode object : list) {
                            if (object instanceof ObjectNode) {
                                itemList.add((ObjectNode) object);
                            }
                        }
                    }
                    break;
                }
                case CREATE_ITEM:{
                    ObjectNode itemJson = AkakariMapper.jsonToObjectNode(item.getJsonObject().get("api_data").toString());
                    if(itemJson == null){
                        LOG.get().warn("CREATE_ITEM itemJson == null");
                        continue;
                    }
                    JsonNode object = itemJson.get("api_slot_item");
                    if (object instanceof ObjectNode) {
                        itemList.add((ObjectNode) object);
                    }
                    break;
                }
                case REMODEL_SLOT:{
                    ObjectNode itemJson = AkakariMapper.jsonToObjectNode(item.getJsonObject().get("api_data").toString());
                    if(itemJson == null){
                        LOG.get().warn("REMODEL_SLOT itemJson == null");
                        continue;
                    }
                    JsonNode object = itemJson.get("api_after_slot");
                    if (object instanceof ObjectNode) {
                        itemList.add((ObjectNode) object);
                    }
                    break;
                }
                case GET_SHIP:{
                    ObjectNode itemJson = AkakariMapper.jsonToObjectNode(item.getJsonObject().get("api_data").toString());
                    if(itemJson == null){
                        LOG.get().warn("GET_SHIP itemJson == null");
                        continue;
                    }
                    JsonNode listObject = itemJson.get("api_slot_item");
                    if(listObject instanceof  ArrayNode) {
                        ArrayNode list = (ArrayNode)listObject;
                        for (JsonNode object : list) {
                            if (object instanceof ObjectNode) {
                                itemList.add((ObjectNode) object);
                            }
                        }
                    }
                    break;
                }
            }
            for(ObjectNode map : itemList){
                int id = JacksonUtil.toInt(map.get("api_id"));
                if(itemIds.contains(id)){
                    if(tmpMap.containsKey(id)){
                        ObjectNode node = tmpMap.get(id);
                        for(Iterator<String> itr = map.fieldNames();itr.hasNext();){
                            String key = itr.next();
                            node.set(key,map.get(key));
                        }
                    }
                    else {
                        tmpMap.put(id, map);
                    }
                }
            }
        }
        ArrayNode result = AkakariMapper.emptyArrayNode();
        for(ObjectNode object : tmpMap.values()){
            result.add(object);
        }
        return result;
    }

    @Nullable
    public static  AkakariSyutsugekiData dataOrNull(AkakariData data,List<AkakariData> startSlotItemArray) {
        AkakariSyutsugekiData akaData = new AkakariSyutsugekiData();
        akaData.date = data.getCreateDate();
        if(akaData.date == null){
            LOG.get().warn("date");
            return null;
        }
        akaData.api_name = data.apiName;

        akaData.body = AkakariMapper.jsonToJsonNode(data.getJsonObject().get("api_data").toString());

        ObjectNode req = AkakariMapper.emptyObjectNode();
        List<String> keyList = Arrays.asList("api_formation", "api_recovery_type", "api_supply_flag", "api_ration_flag", "api_area_id", "api_base_id", "api_squadron_id", "api_item_id", "api_deck_id", "api_maparea_id", "api_mapinfo_no", "api_cell_id");
        for (String key : keyList) {
            if (data.getField(key) != null) {
                req.put(key, data.getField(key));
            }
        }
        if (req.fieldNames().hasNext()) {
            akaData.req = req;
        }
        akaData.slot_item = readSlotItems(akaData.body,startSlotItemArray);

        return akaData;
    }
}
