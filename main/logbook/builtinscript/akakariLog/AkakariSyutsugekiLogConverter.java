package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import logbook.data.DataType;
import logbook.dto.*;
import logbook.util.GsonUtil;
import logbook.util.JacksonUtil;
import org.jetbrains.annotations.Nullable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by noratako5 on 2017/10/22.
 */
public class AkakariSyutsugekiLogConverter {
    private Gson gson = new Gson();
    private BattleExDto tmpBattle;
    private List<BattleExDto> battleList;
    private MapCellDto mapcellDto;
    private ArrayNode ships;
    private Map<String,ItemDto> itemMap;
    private Map<String,ObjectNode> decks;
    private String deckId;
    private int combinedKind;
    private boolean isStart;
    void inputPortData(AkakariSyutsugekiPortData data){
        this.ships = data.ship;
        this.decks = new HashMap<>();
        for(JsonNode node : data.deck) {
            if(!(node instanceof ObjectNode)){
                continue;
            }
            ObjectNode object = (ObjectNode)node;
            this.decks.put(object.get("api_id").asText(),object);
        }
        this.combinedKind = (data.combined_flag != null)?data.combined_flag:0;
        if(this.combinedKind < 0){
            this.combinedKind = 0;
        }
        this.isStart = true;

        Map<String,ItemInfoDto>infoMap = AkakariMasterLogReader.dateToItemInfoDto(data.date);
        if(infoMap == null){
            return;
        }
        Map<String,ItemDto>itemMap = new HashMap<>();
        for(JsonNode json : data.slot_item){
            if(!(json instanceof ObjectNode)){
                continue;
            }
            ObjectNode item = (ObjectNode)json;
            ItemDto itemDto = new ItemDto(infoMap.get(item.get("api_slotitem_id").asText()),Json.createReader(new StringReader(item.toString())).readObject());
            itemMap.put(item.get("api_id").asText(),itemDto);
        }
        this.itemMap = itemMap;
    }
    void inputData(AkakariSyutsugekiData data){
        if(data.body instanceof ObjectNode){
            if (data.body.get("api_ship_data") instanceof ArrayNode) {
                this.ships = (ArrayNode) data.body.get("api_ship_data");
            }
            else if(data.body.get("api_win_rank")!=null){
                this.isStart = false;
                JsonReader jsonreader = Json.createReader(new StringReader(data.body.toString()));
                JsonObject json = jsonreader.readObject();
                if(this.tmpBattle != null) {
                    this.tmpBattle.setResult(json, this.mapcellDto);
                    this.battleList.add(this.tmpBattle);
                    this.tmpBattle = null;
                }
            }
            else if(data.api_name.equals("api_req_map/start") || data.api_name.equals("api_req_map/next")){
                JsonReader jsonreader = Json.createReader(new StringReader(data.body.toString()));
                JsonObject json = jsonreader.readObject();
                this.mapcellDto = new MapCellDto(json,this.isStart);
                if(data.req.get("api_deck_id") != null){
                    this.deckId = data.req.get("api_deck_id").asText();
                }
            }
            else if(data.body.get("api_nowhps") != null){
                DataType dataType = DataType.TYPEMAP.get("/kcsapi/" + data.api_name);
                BattlePhaseKind kind = null;
                switch(dataType){
                    case BATTLE:
                        kind = BattlePhaseKind.BATTLE;
                        break;
                    case BATTLE_MIDNIGHT:
                        kind = BattlePhaseKind.MIDNIGHT;
                        break;
                    case BATTLE_SP_MIDNIGHT:
                        kind = BattlePhaseKind.SP_MIDNIGHT;
                        break;
                    case BATTLE_NIGHT_TO_DAY:
                        kind = BattlePhaseKind.NIGHT_TO_DAY;
                        break;
                    case AIR_BATTLE:
                        kind = BattlePhaseKind.AIR_BATTLE;
                        break;
                    case LD_AIRBATTLE:
                        kind = BattlePhaseKind.LD_AIRBATTLE;
                        break;
                    case COMBINED_AIR_BATTLE:
                        kind = BattlePhaseKind.COMBINED_AIR;
                        break;
                    case COMBINED_LD_AIRBATTLE:
                        kind = BattlePhaseKind.COMBINED_LD_AIR;
                        break;
                    case COMBINED_BATTLE:
                        kind = BattlePhaseKind.COMBINED_BATTLE;
                        break;
                    case COMBINED_BATTLE_MIDNIGHT:
                        kind = BattlePhaseKind.COMBINED_MIDNIGHT;
                        break;
                    case COMBINED_BATTLE_SP_MIDNIGHT:
                        kind = BattlePhaseKind.COMBINED_SP_MIDNIGHT;
                        break;
                    case COMBINED_BATTLE_WATER:
                        kind = BattlePhaseKind.COMBINED_BATTLE_WATER;
                        break;
                    case COMBINED_EC_BATTLE:
                        kind = BattlePhaseKind.COMBINED_EC_BATTLE;
                        break;
                    case COMBINED_EC_BATTLE_MIDNIGHT:
                        kind = BattlePhaseKind.COMBINED_EC_BATTLE_MIDNIGHT;
                        break;
                    case COMBINED_EACH_BATTLE:
                        kind = BattlePhaseKind.COMBINED_EACH_BATTLE;
                        break;
                    case COMBINED_EACH_BATTLE_WATER:
                        kind = BattlePhaseKind.COMBINED_EACH_BATTLE_WATER;
                        break;
                    default:
                        return;
                }
                if(this.tmpBattle == null){
                    this.tmpBattle = new BattleExDto(data.date);
                    this.tmpBattle.setCombinedKind(this.combinedKind);
                    if(this.combinedKind == 0) {
                        DockDto deck1 = new DockDto(this.deckId,this.decks.get(this.deckId).get("api_name").asText(),null);
                        JsonNode deckJson = this.decks.get(this.deckId).get("api_ship");
                        if(!(deckJson instanceof ArrayNode)){
                            return;
                        }
                        ArrayNode shipsNode = (ArrayNode)deckJson;
                        for(JsonNode shipId : shipsNode){
                            ShipDto ship = createShip(shipId.asText(),data.date);
                            if(ship!= null){
                                deck1.getShips().add(ship);
                            }
                        }
                        this.tmpBattle.getFriends().add(deck1);
                    }
                    else{
                        DockDto deck1 = new DockDto("1",this.decks.get("1").get("api_name").asText(),null);
                        {
                            JsonNode deckJson = this.decks.get("1").get("api_ship");
                            if (!(deckJson instanceof ArrayNode)) {
                                return;
                            }
                            ArrayNode shipsNode = (ArrayNode) deckJson;
                            for (JsonNode shipId : shipsNode) {
                                ShipDto ship = createShip(shipId.asText(), data.date);
                                if (ship != null) {
                                    deck1.getShips().add(ship);
                                }
                            }
                        }
                        DockDto deck2 = new DockDto("2",this.decks.get("2").get("api_name").asText(),null);
                        {
                            JsonNode deckJson = this.decks.get("2").get("api_ship");
                            if (!(deckJson instanceof ArrayNode)) {
                                return;
                            }
                            ArrayNode shipsNode = (ArrayNode) deckJson;
                            for (JsonNode shipId : shipsNode) {
                                ShipDto ship = createShip(shipId.asText(), data.date);
                                if (ship != null) {
                                    deck2.getShips().add(ship);
                                }
                            }
                        }
                        if(data.body.get("api_escape_idx") instanceof  ArrayNode){
                            ArrayNode node = (ArrayNode)data.body.get("api_escape_idx");
                            boolean[] escaped = new boolean[6];
                            for(int i=0;i<escaped.length;i++){
                                escaped[i] = false;
                            }
                            for(JsonNode json : node){
                                int index = json.asInt(-1);
                                if(1 <= index && index <= 6){
                                    escaped[index-1] = true;
                                }
                            }
                            deck1.setEscaped(escaped);
                        }
                        if(data.body.get("api_escape_idx_combined") instanceof  ArrayNode){
                            ArrayNode node = (ArrayNode)data.body.get("api_escape_idx_combined");
                            boolean[] escaped = new boolean[6];
                            for(int i=0;i<escaped.length;i++){
                                escaped[i] = false;
                            }
                            for(JsonNode json : node){
                                int index = json.asInt(-1);
                                if(1 <= index && index <= 6){
                                    escaped[index-1] = true;
                                }
                            }
                            deck2.setEscaped(escaped);
                        }
                        this.tmpBattle.getFriends().add(deck1);
                        this.tmpBattle.getFriends().add(deck2);
                    }
                }
                String json = data.body.toString();
                tmpBattle.addPhase2(gson.fromJson(json,LinkedTreeMap.class),json,kind);
            }
            else if(data.body.get("api_deck_data") != null){
                JsonNode node = data.body.get("api_deck_data");
                if(node instanceof ArrayNode){
                    ArrayNode deckArray = (ArrayNode)node;
                    for(JsonNode jsonNode : deckArray){
                        if(!(jsonNode instanceof ObjectNode)){
                            continue;
                        }
                        ObjectNode deck = (ObjectNode)jsonNode;
                        this.decks.put(deck.get("api_id").asText(),deck);
                    }
                }
            }
        }


    }

    @Nullable
    private ShipDto createShip(String id, Date date){
        ObjectNode ship = null;
        for(JsonNode node : this.ships){
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
        JsonNode shipMaster = AkakariMasterLogReader.dateAndIdToShipMasterJson(date,ship.get("api_ship_id").asText());
        if(shipMaster == null){
            return null;
        }
        ShipInfoDto info = new ShipInfoDto(Json.createReader(new StringReader(shipMaster.toString())).readObject());
        return new ShipDto(info,Json.createReader(new StringReader(ship.toString())).readObject(),this.itemMap);
    }


}
