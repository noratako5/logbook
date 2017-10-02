package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.databind.JsonNode;
import logbook.data.AkakariData;
import logbook.data.DataType;
import logbook.internal.LoggerHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by noratako5 on 2017/10/02.
 */
public class AkakariMasterLogRecorder {
    private static LoggerHolder LOG = new LoggerHolder("builtinScript");
    static private String masterLogPath = new File("akakariLog" + File.separator + "master").getAbsolutePath();
    public static void inputData(AkakariData data){
        if(data.getDataType() != DataType.START2){
            return;
        }
        try {
            File dir = new File(masterLogPath);
            if(!dir.exists()){
                if(!dir.mkdirs()){
                    //作成失敗
                    LOG.get().warn("保存失敗");
                    return;
                }
            }
            File file = new File(masterLogPath+File.separator+"master.dat");
            ArrayList<AkakariMasterLog>list = new ArrayList<>();
            if(file.exists() && file.length() > 0){
                AkakariMasterLog[] array = AkakariMapper.readMasterLogFromMessageZstdFile(file);
                if(array == null){
                    LOG.get().warn("ロード失敗");
                    return;
                }
                list.addAll(Arrays.asList(array));
            }
            AkakariMasterLog log = AkakariMasterLog.dataOrNull(data);
            if(log == null){
                return;
            }
            if(list.size() > 0){
                AkakariMasterLog latest = list.get(list.size()-1);
                JsonNode body1 = log.getBody();
                JsonNode body2 = latest.getBody();
                if(body1 != null && body2 != null &&  body1.equals(body2)) {
                    return;
                }
            }
            list.add(log);
            AkakariMasterLog[] result = list.toArray(new AkakariMasterLog[0]);
            AkakariMapper.writeObjectToMessageZstdFile(result,file);
        }
        catch (Exception e){
            LOG.get().warn(data.apiName,e);
        }
    }
}
