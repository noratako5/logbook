package logbook.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;

/**
 * Created by noratako5 on 2017/09/30.
 */
public class JacksonUtil {

    /**nullなら-1、NumberならintValue()、その他ならparseInt(toString())、整数でない文字列で落ちる*/
    public static int toInt(JsonNode object){
        if(object == null || object instanceof NullNode || object.isContainerNode()){
            return -1;
        }else if(object instanceof NumericNode){
            return ((NumericNode)object).asInt();
        }else{
            //数値はある日突然文字列型になることがよくある
            return Integer.parseInt(object.toString().replace("\"",""));
        }
    }
    public static int[] toIntArray(JsonNode object){
        if(object instanceof ArrayNode){
            ArrayNode list = (ArrayNode)object;
            int[] result = new int[list.size()];
            for(int i=0;i<list.size();i++){
                result[i] = toInt(list.get(i));
            }
            return result;
        }else{
            return null;
        }
    }

}
