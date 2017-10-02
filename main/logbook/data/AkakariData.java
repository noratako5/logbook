package logbook.data;

import logbook.dto.AbstractDto;

import javax.json.JsonObject;
import java.util.Date;
import java.util.Map;

/**
 * Created by noratako5 on 2017/10/02.
 */
public final class AkakariData extends AbstractDto implements Data {
    private final DataType type;
    private final Date date;
    private final JsonObject json;
    private final Map<String, String> postField;
    public final String apiName;

    public AkakariData(DataType type, Date createDate, JsonObject json, Map<String, String> postField,String apiName) {
        this.type = type;
        this.date = createDate;
        this.json = json;
        this.postField = postField;
        this.apiName = apiName;
    }

    @Override
    public DataType getDataType() {
        return this.type;
    }

    @Override
    public Date getCreateDate() {
        return this.date;
    }

    @Override
    public JsonObject getJsonObject() {
        return this.json;
    }

    @Override
    public String getField(String key) {
        if (this.postField != null) {
            return this.postField.get(key);
        }
        return null;
    }
}
