package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.node.NumericNode;
import com.google.gson.Gson;
import logbook.data.AkakariData;
import logbook.data.Data;
import logbook.internal.LoggerHolder;
import logbook.util.JacksonUtil;
import org.jetbrains.annotations.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;

/**
 * Created by noratako5 on 2017/09/18.
 */
public class AkakariSyutsugekiPortData {
    private static LoggerHolder LOG = new LoggerHolder("builtinScript");

    //出撃時と帰投時の2点取る
    //マスタデータは出撃ログとは別の所で保存。(当分使う予定ないが赤仮ログ単体で出力する時に使う)
    //deckに入ってる艦(支援あるので出撃しない艦含む)の装備含む情報

    ///portの受信時刻。自然回復を見る時に使う。
    public Date date;
    ///連合フラグ
    public Integer combined_flag;
    ///ギミック解除音フラグが入ることがよくあるらしい。何がどう格納されるか予測できないのでそのまま残す。
    public JsonNode event_object;
    ///枯渇洋上補給、枯渇空襲被害などで稀に必要になる資源情報。一応開発資材とか海域で拾う時にカンスト状態で何か挙動あるかもしれないので全種残す。提督IDとかは消す。
    public ArrayNode material;
    ///出撃用のログなので司令部レベルだけ残す。
    public ObjectNode basic;
    ///遠征状態は出撃画面から母港挟まずに操作可能なので母港の情報が最新とは限らない
    public ArrayNode deck;
    ///deckに入ってるやつだけフィルタする。
    public ArrayNode ship;
    ///shipの艦が装備してるやつだけフィルタする。基地航空はイベント海域が絡むと面倒で別途保存するのでここじゃない。
    public ArrayNode slot_item;

    ///デッキ情報読み取り
    @Nullable
    private static ArrayNode readDeck(ObjectNode portJson){
        JsonNode deckObject = portJson.get("api_deck_port");
        if (deckObject instanceof ArrayNode) {
            ArrayNode deckList = (ArrayNode) deckObject;
            ArrayNode result = AkakariMapper.emptyArrayNode();
            for (JsonNode item : deckList) {
                if (item instanceof ObjectNode) {
                    ObjectNode itemMap = (ObjectNode)item;
                    ObjectNode map = AkakariMapper.emptyObjectNode();
                    List<String> keyList = Arrays.asList("api_id", "api_name", "api_mission", "api_ship");
                    for(Iterator<String> itr = itemMap.fieldNames();itr.hasNext();){
                        String key = itr.next();
                        if(keyList.contains(key)){
                            map.set(key,itemMap.get(key));
                        }
                    }
                    result.add(map);
                }
            }
            return result;
        } else {
            return null;
        }
    }
    ///デッキに所属している艦情報読み取り
    @Nullable
    private static ArrayNode readShips(ArrayNode deck, ObjectNode portJson){
        ///デッキに入ってる艦のID
        List<Integer> shipIds = new ArrayList<>();
        for (JsonNode item : deck) {
            int[] shipArray = JacksonUtil.toIntArray(item.get("api_ship"));
            if(shipArray != null) {
                for (int id : shipArray) {
                    if (id > 0) {
                        shipIds.add(id);
                    }
                }
            }
        }
        JsonNode shipObject = portJson.get("api_ship");
        if (shipObject instanceof ArrayNode) {
            ArrayNode shipList = (ArrayNode) shipObject;
            ArrayNode result = AkakariMapper.emptyArrayNode();
            for (JsonNode item : shipList) {
                if (item instanceof ObjectNode) {
                    ObjectNode itemMap = (ObjectNode) item;
                    int id = JacksonUtil.toInt(itemMap.get("api_id"));
                    if (shipIds.contains(id)) {
                        result.add(itemMap);
                    }
                }
            }
            return result;
        } else {
            return null;
        }
    }
    ///デッキに所属している艦が装備している装備読み取り
    @Nullable
    private static ArrayNode readSlotItems(ArrayNode ships, ObjectNode portJson, List<AkakariData> startSlotItemArray){
        ///デッキに入ってる艦の装備のID。拡張穴含む
        List<Integer> itemIds = new ArrayList<>();
        for (JsonNode item : ships) {
            int[] slotArray = JacksonUtil.toIntArray(item.get("api_slot"));
            if(slotArray != null) {
                for (int id : slotArray) {
                    if (id > 0) {
                        itemIds.add(id);
                    }
                }
            }
            int exSlot = JacksonUtil.toInt(item.get("api_slot_ex"));
            if (exSlot > 0) {
                itemIds.add(exSlot);
            }
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
    private static ObjectNode readBasic(ObjectNode portJson){
        JsonNode basicObject = portJson.get("api_basic");
        if(basicObject instanceof ObjectNode){
            ObjectNode basicMap = (ObjectNode)basicObject;
            ObjectNode result = AkakariMapper.emptyObjectNode();
            List<String> keyList = Arrays.asList("api_level");
            for (Iterator<String> itr = basicMap.fieldNames();itr.hasNext();) {
                String key = itr.next();
                if (keyList.contains(key)) {
                    result.set(key,basicMap.get(key));
                }
            }
            return result;
        }
        return null;
    }

    @Nullable
    private static ArrayNode readMaterial(ObjectNode portJson){
        JsonNode materialObject = portJson.get("api_material");
        if(materialObject instanceof ArrayNode){
            ArrayNode materialList = (ArrayNode) materialObject;
            ArrayNode result = AkakariMapper.emptyArrayNode();
            for(JsonNode object : materialList){
                if(object instanceof ObjectNode){
                    ObjectNode map = (ObjectNode)object;
                    int id = JacksonUtil.toInt(map.get("api_id"));
                    int material = JacksonUtil.toInt(map.get("api_value"));
                    ObjectNode node = AkakariMapper.emptyObjectNode();
                    node.put("api_id",id);
                    node.put("api_value",material);
                    result.add(node);
                }
            }
            return result;
        }
        return null;
    }

    @Nullable
    public static AkakariSyutsugekiPortData dataOrNull(List<AkakariData> startSlotItemArray, AkakariData startPort) {
        AkakariSyutsugekiPortData port = new AkakariSyutsugekiPortData();
        port.date = startPort.getCreateDate();
        if(port.date == null){
            LOG.get().warn("port date error");
            return null;
        }
        ObjectNode portJson = AkakariMapper.jsonToObjectNode(startPort.getJsonObject().get("api_data").toString());
        if (portJson == null) {
            LOG.get().warn("portJson == null");
            return null;
        }
        port.deck = readDeck(portJson);
        if(port.deck == null){
            LOG.get().warn("port.deck == null");
            return null;
        }
        port.ship = readShips(port.deck,portJson);
        if(port.ship == null){
            LOG.get().warn("port.ship == null");
            return null;
        }
        port.slot_item = readSlotItems(port.ship,portJson,startSlotItemArray);
        if(port.slot_item == null){
            LOG.get().warn("port.slot_item == null");
            return null;
        }
        port.basic = readBasic(portJson);
        if(port.basic == null){
            LOG.get().warn("port.basic == null");
            return null;
        }
        port.material = readMaterial(portJson);
        if(port.material == null){
            LOG.get().warn("port_material == null");
            return null;
        }
        port.event_object = portJson.get("api_event_object");

        if(portJson.get("api_combined_flag") != null){
            port.combined_flag = JacksonUtil.toInt(portJson.get("api_combined_flag"));
        }
        return port;
    }



}