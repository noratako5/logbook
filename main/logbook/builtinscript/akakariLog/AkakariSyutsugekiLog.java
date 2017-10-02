package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import logbook.data.AkakariData;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.internal.LoggerHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
