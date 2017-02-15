package logbook.extraLog;

import com.dyuproject.protostuff.Tag;

import java.util.Date;

public class JsonLog {
    @Tag(1)
    final Date jsonDate;
    @Tag(2)
    final String apiName;
    @Tag(3)
    final String json;

    public JsonLog(Date date,String apiName,String json){
        this.jsonDate = date;
        this.apiName = apiName;
        this.json = json;
    }
}
