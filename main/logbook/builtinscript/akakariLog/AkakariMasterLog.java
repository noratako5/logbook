package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import logbook.data.AkakariData;
import logbook.data.DataType;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Created by noratako5 on 2017/10/02.
 */
public class AkakariMasterLog {
    public Date date;
    public byte[] zstd_body;

    @Nullable
    public static AkakariMasterLog dataOrNull(AkakariData data){
        if(data.getDataType() != DataType.START2){
            return null;
        }
        AkakariMasterLog log = new AkakariMasterLog();
        log.date = data.getCreateDate();
        JsonNode body = AkakariMapper.jsonToJsonNode(data.getJsonObject().get("api_data").toString());
        if(body == null){
            return null;
        }
        log.zstd_body = AkakariMapper.objectToMessageZstdBytes(body);
        if(log.date == null || log.zstd_body == null){
            return null;
        }
        return log;
    }
    @JsonIgnore
    public JsonNode getBody(){
        return AkakariMapper.messageZstdToJsonNode(this.zstd_body);
    }
}
