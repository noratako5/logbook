/**
 *
 */
package logbook.util;

import java.util.List;

public class GsonUtil {

    /**objectがNumberならString.valueOf(intValue())、nullならnull、他toString()*/
    public static String toIntString(Object object){
        if(object == null){
            return null;
        }else if(object instanceof Number){
            return String.valueOf(((Number)object).intValue());
        }else{
            return object.toString();
        }
    }
    /**nullなら-1、NumberならintValue()、その他ならparseInt(toString())、整数でない文字列で落ちる*/
    public static int toInt(Object object){
        if(object == null){
            return -1;
        }else if(object instanceof Number){
            return ((Number)object).intValue();
        }else{
            return Integer.parseInt(object.toString());
        }
    }
    public static int[] toIntArray(Object object){
        if(object instanceof List){
            List<Object> list = (List<Object>)object;
            int[] result = new int[list.size()];
            for(int i=0;i<list.size();i++){
                result[i] = toInt(list.get(i));
            }
            return result;
        }else{
            return null;
        }
    }
    public static int[][] toIntArrayArray(Object object){
        if(object instanceof List){
            List<Object> list = (List<Object>)object;
            int[][] result = new int[list.size()][];
            for(int i=0;i<list.size();i++){
                result[i] = toIntArray(list.get(i));
            }
            return result;
        }else{
            return null;
        }
    }
    /**nullなら-1、NumberならintValue()、その他ならparseInt(toString())、整数でない文字列で落ちる*/
    public static long toLong(Object object){
        if(object == null){
            return -1;
        }else if(object instanceof Number){
            return ((Number)object).longValue();
        }else{
            return Long.parseLong(object.toString());
        }
    }
    /**nullなら-1、NumberならdoubleValue()、その他ならparseDouble(toString())、変換できない文字列で落ちる*/
    public static double toDouble(Object object){
        if(object == null){
            return -1;
        }else if(object instanceof Number){
            return ((Number)object).doubleValue();
        }else{
            return Double.parseDouble(object.toString());
        }
    }
    public static double[] toDoubleArray(Object object){
        if(object instanceof List){
            List<Object> list = (List<Object>)object;
            double[] result = new double[list.size()];
            for(int i=0;i<list.size();i++){
                result[i] = toDouble(list.get(i));
            }
            return result;
        }else{
            return null;
        }
    }
    public static double[][] toDoubleArrayArray(Object object){
        if(object instanceof List){
            List<Object> list = (List<Object>)object;
            double[][] result = new double[list.size()][];
            for(int i=0;i<list.size();i++){
                result[i] = toDoubleArray(list.get(i));
            }
            return result;
        }else{
            return null;
        }
    }
}
