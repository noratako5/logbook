package logbook.builtinscript.akakariLog;

import logbook.data.AkakariData;
import logbook.data.Data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import logbook.internal.LoggerHolder;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by noratako5 on 2017/09/13.
 */

///出撃時のログを保存する。基地系ログ、羅針盤ログ、戦闘後ステータスログなど現状の戦闘単体の記録のみで不足する案件に使う。
public class AkakariSyutsugekiLogRecorder {
    private static LoggerHolder LOG = new LoggerHolder("builtinScript");
    //保存はmessagepack。
    //battlelog同様起動時に全て読み込み、戦闘日時と直後の艦情報のペアは持っておく。

    static private String syutsugekiLogPath = new File("akakariLog" + File.separator + "syutsugeki").getAbsolutePath();

    ///slotitemMember取得時点でリセット。
    ///糞重いので必要な装備だけ保存する。
    ///将来的には赤仮ログ単独でのcsv出力やるので保存しておくが、当分はbattlelog側の情報そのまま使うのでなくてもよい部分
    private static List<AkakariData> startSlotItemArray = new ArrayList<>();
    private static  AkakariData startPort = null;
    private static AkakariSyutsugekiLog log = null;

    public static void inputData(AkakariData data){
        try {
            if (log == null) {
                log = AkakariSyutsugekiLog.dataOrNull(startSlotItemArray, startPort);
            }
            switch (data.getDataType()) {
                case PORT:
                    startPort = data;
                    break;
                case REQUIRE_INFO:
                case SLOTITEM_MEMBER:
                    startSlotItemArray = new ArrayList<>();
                    startSlotItemArray.add(data);
                    break;
                case CREATE_ITEM:
                case REMODEL_SLOT:
                case GET_SHIP:
                    startSlotItemArray.add(data);
                    break;
            }
            if (log != null) {
                log.inputData(data);
                if (log.isFinish()) {
                    if (log.needSave) {
                        saveData(log);
                    }
                    log = null;
                }
            }
        }
        catch (Exception e){
            LOG.get().warn(data.apiName,e);
        }
    }

    private static void saveData(AkakariSyutsugekiLog log){
        File dir = new File(syutsugekiLogPath);
        if(!dir.exists()){
            if(!dir.mkdirs()){
                //作成失敗
                LOG.get().warn("保存失敗");
                return;
            }
        }
        try {
            FastDateFormat format =  FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("JST"));
            String date = format.format(log.start_port.date);
            File file = new File(syutsugekiLogPath+File.separator+"syutsugeki"+date+".dat");
            ArrayList<AkakariSyutsugekiLog> list = new ArrayList<>();
            if(file.exists() && file.length() > 0){
                AkakariSyutsugekiLog[] array = AkakariMapper.readSyutsugekiLogFromMessageZstdFile(file);
                if(array == null){
                    LOG.get().warn("ロード失敗");
                    File file2 = new File(syutsugekiLogPath+File.separator+"syutsugeki"+date+"_error.dat");
                    file.renameTo(file2);
                }
                else {
                    list.addAll(Arrays.asList(array));
                }
            }
            list.add(log);
            AkakariSyutsugekiLog[] result = list.toArray(new AkakariSyutsugekiLog[0]);
            AkakariSyutsugekiLogReader.updateLogFile(file.toPath(),result);
            AkakariMapper.writeObjectToMessageZstdFile(result,file);
            AkakariSyutsugekiLogReader.loadStartPortDate(log);
            //AkakariMapper.writeObjectToJsonFile(result,new File(syutsugekiLogPath+File.separator+"syutsugeki"+date+".json"));
        }
        catch (Exception e){
            LOG.get().warn("保存失敗",e);
        }
    }
    public static void createJson(File file){
        try {
            ArrayList<AkakariSyutsugekiLog> list = new ArrayList<>();
            if(file.exists() && file.length() > 0){
                AkakariSyutsugekiLog[] array = AkakariMapper.readSyutsugekiLogFromMessageZstdFile(file);
                if(array != null){
                    list.addAll(Arrays.asList(array));
                }
            }
            AkakariSyutsugekiLog[] result = list.toArray(new AkakariSyutsugekiLog[0]);
            AkakariMapper.writeObjectToJsonFile(result,new File(file.getAbsolutePath()+".json"));
        }
        catch (Exception e){
            LOG.get().warn("保存失敗",e);
        }
    }
    @NotNull
    public static Path dateToPath(Date startPortDate){
        FastDateFormat format =  FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("JST"));
        String date = format.format(startPortDate);
        File file = new File(syutsugekiLogPath+File.separator+"syutsugeki"+date+".dat");
        return file.toPath();
    }

    @Nullable
    public static List<Path> allFilePath(){
        Path path = (new File(syutsugekiLogPath)).toPath();
        if(Files.exists(path)) {
            try {
                return Files.list(path).collect(Collectors.toList());
            } catch (Exception e) {
                LOG.get().warn("読み込み失敗", e);
                return null;
            }
        }
        else {
            return null;
        }
    }
}
