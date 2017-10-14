package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import logbook.util.JacksonUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by noratako5 on 2017/10/13.
 */
public class AkakariSyutsugekiAirBaseData {
    public ArrayNode airBase;
    public ArrayNode slot_item;
    @Nullable
    public ObjectNode getPlaneInfo(int areaId,int baseId,int squadronId){
        ObjectNode airBaseNode = null;
        for(JsonNode jsonNode : this.airBase){
            if(!(jsonNode instanceof ObjectNode)){
                continue;
            }
            ObjectNode node = (ObjectNode)jsonNode;
            if(JacksonUtil.toInt(node.get("api_area_id")) == areaId && JacksonUtil.toInt(node.get("api_rid")) == baseId){
                airBaseNode = node;
                break;
            }
        }
        if(airBaseNode == null){
            return null;
        }
        JsonNode jsonInfo = airBaseNode.get("api_plane_info");
        if(!(jsonInfo instanceof ArrayNode)){
            return null;
        }
        ArrayNode infoArray = (ArrayNode)jsonInfo;
        for(JsonNode jsonNode : infoArray){
            if(!(jsonNode instanceof ObjectNode)){
                continue;
            }
            ObjectNode node = (ObjectNode)jsonNode;
            if(JacksonUtil.toInt(node.get("api_squadron_id")) == squadronId){
                return node;
            }
        }
        return null;
    }
    @Nullable
    public ObjectNode getItem(int areaId,int baseId,int squadronId){
        ObjectNode planeInfo = getPlaneInfo(areaId,baseId,squadronId);
        if(planeInfo == null){
            return null;
        }
        int slotId = JacksonUtil.toInt(planeInfo.get("api_slotid"));
        if(slotId <= 0){
            return null;
        }
        for(JsonNode jsonItem : this.slot_item){
            if(!(jsonItem instanceof ObjectNode)){
                continue;
            }
            ObjectNode item = (ObjectNode)jsonItem;
            if(JacksonUtil.toInt(item.get("api_id"))==slotId){
                return item;
            }
        }
        return null;
    }
}
