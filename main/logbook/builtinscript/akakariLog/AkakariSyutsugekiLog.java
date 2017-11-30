package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import logbook.data.AkakariData;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.dto.ItemDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.internal.LoggerHolder;
import logbook.util.JacksonUtil;
import org.jetbrains.annotations.Nullable;

import javax.json.Json;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by noratako5 on 2017/09/18.
 */
public class AkakariSyutsugekiLog {
    private static LoggerHolder LOG = new LoggerHolder("builtinScript");
    public AkakariSyutsugekiPortData start_port;
    public AkakariSyutsugekiPortData end_port;
    public AkakariSyutsugekiData[] data;
    @JsonIgnore
    private List<AkakariData> startSlotItemArray;
    @JsonIgnore
    private List<AkakariSyutsugekiData> dataList = new ArrayList<>();
    @JsonIgnore
    private AkakariData endPortData;
    @JsonIgnore
    private AkakariData endSlotItemData;
    @JsonIgnore
    private boolean start = false;
    @JsonIgnore
    public boolean needSave = false;

    @JsonIgnore
    private AkakariSyutsugekiAirBaseData firstAirBase;
    @JsonIgnore
    private AkakariSyutsugekiAirBaseData lastAirBase;
    @JsonIgnore
    private Map<String,ItemDto>itemMap;


    ///情報不足とかあったらnull返す
    @Nullable
    public static AkakariSyutsugekiLog dataOrNull(List<AkakariData> startSlotItemArray, AkakariData startPort){
        if(startPort == null) {
            LOG.get().warn("startPort == null");
            return null;
        }
        AkakariSyutsugekiLog data = new AkakariSyutsugekiLog();
        //基地航空はイベント海域が絡み情報来るタイミングが読めないので来た時に重複気にせず都度保存する。大した量ではないので
        data.startSlotItemArray = startSlotItemArray;
        data.start_port = AkakariSyutsugekiPortData.dataOrNull(startSlotItemArray,startPort);
        if(data.start_port == null){
            LOG.get().warn("start_port failed");
            return null;
        }
        return data;
    }

    public void inputData(AkakariData data) {
        DataType type = data.getDataType();
        switch (type){
            case PORT:
                this.endPortData = data;
                break;
            case SLOTITEM_MEMBER:
            case REQUIRE_INFO:
                this.endSlotItemData = data;
                break;
            default:
                AkakariSyutsugekiData akaData = AkakariSyutsugekiData.dataOrNull(data,this.startSlotItemArray);
                if(akaData != null){
                    this.dataList.add(akaData);
                }
        }
        if(type == DataType.MAPINFO){
            this.needSave = true;
        }
        if(type == DataType.START){
            this.start = true;
        }
        if(this.endPortData != null && !start){
            //出撃せずに母港に戻った場合
            this.end_port = AkakariSyutsugekiPortData.dataOrNull(startSlotItemArray,endPortData);
        }
        else if(this.endPortData != null && this.endSlotItemData != null){
            List<AkakariData> list = new ArrayList<>();
            list.add(this.endSlotItemData);
            this.end_port = AkakariSyutsugekiPortData.dataOrNull(list,endPortData);
        }
        if(isFinish()){
            this.data = dataList.toArray(new AkakariSyutsugekiData[0]);
        }
    }

    @JsonIgnore
    public boolean isFinish(){
        return (this.end_port != null);
    }

    @JsonIgnore
    public List<Date> getBattleDateList(){
        List<Date>result = new ArrayList<>();
        for(AkakariSyutsugekiData data : this.data){
            if(data.body instanceof  ObjectNode && data.body.get("api_nowhps")!=null){
                result.add(data.date);
            }
        }
        return result;
    }

    @JsonIgnore
    public ArrayNode shipsAfterBattle(Date battleDate){
        for(AkakariSyutsugekiData data : this.data){
            if(data.date.after(battleDate) &&  data.body instanceof  ObjectNode){
                JsonNode node = data.body.get("api_ship_data");
                if(node instanceof ArrayNode) {
                    return (ArrayNode)node;
                }
            }
        }
        return this.end_port.ship;
    }
    ///出撃してた場合出撃海域
    @JsonIgnore
    public int areaId(){
        for(AkakariSyutsugekiData data : this.data){
            if(data.api_name.equals("api_req_map/start")){
                return JacksonUtil.toInt(data.body.get("api_maparea_id"));
            }
        }
        return -1;
    }
    ///補給などの操作を加える前の初期基地航空
    @JsonIgnore
    public AkakariSyutsugekiAirBaseData firstAirBase(){
        if(this.firstAirBase != null){
            return this.firstAirBase;
        }
        for(AkakariSyutsugekiData data : this.data){
            if(data.api_name.equals("api_get_member/mapinfo")){
                JsonNode node = data.body.get("api_air_base");
                if(node instanceof ArrayNode){
                    AkakariSyutsugekiAirBaseData airBaseData = new AkakariSyutsugekiAirBaseData();
                    airBaseData.airBase = (ArrayNode)node;
                    airBaseData.slot_item = data.slot_item;
                    this.firstAirBase = airBaseData;
                    return airBaseData;
                }
                else{
                    return null;
                }
            }
        }
        return null;
    }
    ///出撃直前の基地航空
    @JsonIgnore
    public AkakariSyutsugekiAirBaseData lastAirBase(){
        if(this.lastAirBase != null){
            return this.lastAirBase;
        }
        AkakariSyutsugekiAirBaseData airBase = this.firstAirBase();
        if(airBase == null || airBase.airBase == null || airBase.slot_item == null){
            return null;
        }
        for(AkakariSyutsugekiData data : this.data){
            ObjectNode req = data.req;
            if(req == null){
                continue;
            }
            if(data.body == null){
                continue;
            }
            if(req.get("api_area_id")!=null && req.get("api_base_id")!=null){
                int areaId = JacksonUtil.toInt(req.get("api_area_id"));
                int baseId = JacksonUtil.toInt(req.get("api_base_id"));
                if(areaId <0 || baseId < 0){
                    continue;
                }
                JsonNode info = data.body.get("api_plane_info");
                if(!(info instanceof ArrayNode)){
                    continue;
                }
                ArrayNode infoArray = (ArrayNode)info;
                ArrayNode oldInfo = null;
                for(JsonNode jsonNode:airBase.airBase){
                    if(!(jsonNode instanceof ObjectNode)){
                        continue;
                    }
                    ObjectNode node = (ObjectNode)jsonNode;
                    if(JacksonUtil.toInt(node.get("api_area_id")) == areaId && JacksonUtil.toInt(node.get("api_rid"))==baseId){
                        JsonNode jsonInfo = node.get("api_plane_info");
                        if(jsonInfo instanceof ArrayNode){
                            oldInfo = (ArrayNode)jsonInfo;
                        }
                    }
                }
                if(oldInfo == null){
                    return null;
                }
                for(JsonNode jsonNode : infoArray){
                    if(!(jsonNode instanceof ObjectNode)){
                        continue;
                    }
                    ObjectNode node = (ObjectNode)jsonNode;
                    for(int i=0;i<oldInfo.size();i++){
                        JsonNode oldJsonNode = oldInfo.get(i);
                        if(!(oldJsonNode instanceof ObjectNode)){
                            continue;
                        }
                        ObjectNode oldNode = (ObjectNode)oldJsonNode;
                        if(JacksonUtil.toInt(node.get("api_squadron_id")) == JacksonUtil.toInt(oldNode.get("api_squadron_id"))){
                            oldInfo.set(i,node);
                            break;
                        }
                    }
                }
                if(data.slot_item != null) {
                    for (JsonNode item : data.slot_item){
                        //同一装備の重複が発生するが出撃マップ選択画面内での更新処理は無い(はず)なので気にしなくていい
                        airBase.slot_item.add(item);
                    }
                }
            }
            if(data.api_name.equals("api_req_map/start")){
                break;
            }
        }
        this.lastAirBase = airBase;
        return airBase;
    }

    @JsonIgnore
    public ShipDto shipIdToStartShip(String id){
        ObjectNode ship = null;
        for(JsonNode node : this.start_port.ship){
            if(!(node instanceof ObjectNode)){
                continue;
            }
            ObjectNode objectNode = (ObjectNode)node;
            if(objectNode.get("api_id").asText().equals(id)){
                ship = objectNode;
                break;
            }
        }
        if(ship == null){
            return null;
        }
        JsonNode shipMaster = AkakariMasterLogReader.dateAndIdToShipMasterJson(this.start_port.date,ship.get("api_ship_id").asText());
        if(shipMaster == null){
            return null;
        }

        Map<String,ItemInfoDto>infoMap = AkakariMasterLogReader.dateToItemInfoDto(this.start_port.date);
        if(infoMap == null){
            return null;
        }
        if(this.itemMap == null) {
            Map<String, ItemDto> itemMap = new HashMap<>();
            for (JsonNode json : this.start_port.slot_item) {
                if (!(json instanceof ObjectNode)) {
                    continue;
                }
                ObjectNode item = (ObjectNode) json;
                ItemDto itemDto = new ItemDto(infoMap.get(item.get("api_slotitem_id").asText()), Json.createReader(new StringReader(item.toString())).readObject());
                itemMap.put(item.get("api_id").asText(), itemDto);
            }
            this.itemMap = itemMap;
        }

        ShipInfoDto info = new ShipInfoDto(Json.createReader(new StringReader(shipMaster.toString())).readObject());
        return new ShipDto(info,Json.createReader(new StringReader(ship.toString())).readObject(),this.itemMap);
    }
}
