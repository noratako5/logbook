/**
 * 
 */
package logbook.dto;

import java.util.Date;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.data.context.GlobalContext;
import logbook.util.JsonUtils;

import com.dyuproject.protostuff.Tag;

/**
 * １回の会敵情報
 * @author Nekopanda
 */
public class BattleExDto extends BattleExBaseDto {

    @Tag(52)
    private String basicJson;

    @Tag(53)
    private DockDto support;

    public BattleExDto(Date date) {
        super(date);
    }

    /**
     * 母港情報を設定
     * @param object
     * @param shipSpace
     * @param itemSpace
     */
    @Override
    public void setBasicInfo(JsonObject object, int shipSpace, int itemSpace) {
        super.setBasicInfo(object, shipSpace, itemSpace);
        this.basicJson = JsonUtils.toString(object);
    }

    /**
     * 戦闘フェーズ結果を読み込む
     * @param object 受け取ったJSON
     * @param kind 戦闘の種別
     * @return 作成されたPhaseオブジェクト
     */
    @Override
    public Phase addPhase(JsonObject object, BattlePhaseKind kind) {
        Phase phase = super.addPhase(object, kind);
        if (this.getPhaseList().size() == 0) {
            JsonNumber support_flag = object.getJsonNumber("api_support_flag");
            if ((support_flag != null) && (support_flag.intValue() != 0)) {
                JsonObject support = object.getJsonObject("api_support_info");
                JsonValue support_hourai = support.get("api_support_hourai");
                JsonValue support_air = support.get("api_support_airatack");
                if ((support_hourai != null) && (support_hourai != JsonValue.NULL)) {
                    JsonValue support_deck_id = ((JsonObject) support_hourai).get("api_deck_id");
                    this.support = GlobalContext.getDock(support_deck_id.toString());
                }
                else if ((support_air != null) && (support_air != JsonValue.NULL)) {
                    JsonValue support_deck_id = ((JsonObject) support_air).get("api_deck_id");
                    this.support = GlobalContext.getDock(support_deck_id.toString());
                }
            }
        }
        return phase;
    }

    public JsonObject getBasicJson() {
        return JsonUtils.fromString(this.basicJson);
    }

    public String getBasicJsonString() {
        return this.basicJson;
    }

    /**
     * 支援艦隊
     * @return
     */
    public DockDto getDockSupport() {
        return this.support;
    }
}