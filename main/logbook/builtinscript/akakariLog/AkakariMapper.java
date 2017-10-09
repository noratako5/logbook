package logbook.builtinscript.akakariLog;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import logbook.internal.LoggerHolder;
import org.jetbrains.annotations.Nullable;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import com.github.luben.zstd.Zstd;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.time.ZoneId;
import java.util.TimeZone;

import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Created by noratako5 on 2017/09/23.
 */
public class AkakariMapper {
    private static LoggerHolder LOG = new LoggerHolder("builtinScript");
    static private final ObjectMapper msgMapper = new ObjectMapper(new MessagePackFactory());
    static {
        msgMapper.registerModule(new AfterburnerModule());
        msgMapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")));
        //null値は書き出さない
        msgMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //未知のフィールドを含む新しいバージョンのログを旧バージョンで読んで上書きするとデータ消えるので禁止
        msgMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }
    static private final ObjectMapper jsonMapper = new ObjectMapper();
    static {
        jsonMapper.registerModule(new AfterburnerModule());
        jsonMapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Tokyo")));
        //null値は書き出さない
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //現状未知のJsonについてはJsonNode以外にマッピングする場面ないからあまり関係ない
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Nullable
    public static JsonNode jsonToJsonNode(String json){
        try {
            return jsonMapper.readTree(json);
        } catch (Exception e) {
            LOG.get().warn("mapping",e);
            return null;
        }
    }
    @Nullable
    public static ObjectNode jsonToObjectNode(String json){
        JsonNode node = jsonToJsonNode(json);
        if(node instanceof ObjectNode){
            return (ObjectNode)node;
        }
        else{
            return null;
        }
    }
    @Nullable
    public static ArrayNode jsonToArrayNode(String json){
        JsonNode node = jsonToJsonNode(json);
        if(node instanceof ArrayNode){
            return (ArrayNode)node;
        }
        else{
            return null;
        }
    }
    public static ObjectNode emptyObjectNode(){
        return jsonMapper.createObjectNode();
    }
    public static ArrayNode emptyArrayNode(){
        return jsonMapper.createArrayNode();
    }
    public static byte[] objectToJsonBytes(Object object){
        try {
            return jsonMapper.writeValueAsBytes(object);
        }catch (Exception e){
            LOG.get().warn("mapping",e);
            return null;
        }
    }
    public static boolean writeObjectToJsonFile(Object object,File file){
        try {
            jsonMapper.writeValue(file, object);
            return true;
        }
        catch (Exception e){
            LOG.get().warn("save",e);
            return false;
        }
    }
    @Nullable
    public static AkakariSyutsugekiLog[] readSyutsugekiLogFromJsonFile(File file){
        try {
            return jsonMapper.readValue(file,AkakariSyutsugekiLog[].class);
        }
        catch (Exception e) {
            LOG.get().warn("load", e);
            return null;
        }
    }

    @Nullable
    public static byte[] objectToMessageZstdBytes(Object object){
        try {
            byte[] raw = msgMapper.writeValueAsBytes(object);
            return Zstd.compress(raw);
        }catch (Exception e){
            LOG.get().warn("mapping",e);
            return null;
        }
    }

    public static boolean writeObjectToMessageZstdFile(Object object, File file){
        try {
            byte[] compressed = objectToMessageZstdBytes(object);
            if(compressed == null){
                return false;
            }
            Files.write(file.toPath(),compressed, CREATE);
            return true;
        }
        catch (Exception e){
            LOG.get().warn("save",e);
            return false;
        }
    }

    @Nullable
    public static JsonNode messageZstdToJsonNode(byte[] compressed){
        try {
            byte[] raw = Zstd.decompress(compressed,(int)Zstd.decompressedSize(compressed));
            return msgMapper.readTree(raw);
        }
        catch (Exception e) {
            LOG.get().warn("load failed", e);
            return null;
        }
    }

    @Nullable
    public static AkakariSyutsugekiLog[] readSyutsugekiLogFromMessageZstdFile(File file){
        try {
            byte[] compressed = Files.readAllBytes(file.toPath());
            byte[] raw = Zstd.decompress(compressed,(int)Zstd.decompressedSize(compressed));
            return msgMapper.readValue(raw,AkakariSyutsugekiLog[].class);
        }
        catch (Exception e) {
            LOG.get().warn("load failed", e);
            return null;
        }
    }
    @Nullable
    public static AkakariMasterLog[] readMasterLogFromMessageZstdFile(File file){
        try {
            byte[] compressed = Files.readAllBytes(file.toPath());
            byte[] raw = Zstd.decompress(compressed,(int)Zstd.decompressedSize(compressed));
            return msgMapper.readValue(raw,AkakariMasterLog[].class);
        }
        catch (Exception e) {
            LOG.get().warn("load failed", e);
            return null;
        }
    }
}
