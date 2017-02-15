package logbook.extraLog;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.Tag;

import java.util.ArrayList;
import java.util.List;

public class SortieLog {
    @Tag(1)
    final List<JsonLog> jsonList;
    public  SortieLog(){
        this.jsonList = new ArrayList<>();
    }
}
