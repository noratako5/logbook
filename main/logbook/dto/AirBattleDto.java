/**
 *
 */
package logbook.dto;

import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.dyuproject.protostuff.Tag;
import com.google.gson.internal.LinkedTreeMap;

import logbook.internal.Item;
import logbook.util.GsonUtil;
import logbook.util.JsonUtils;

/**
 * @author Nekopanda
 *
 */
public class AirBattleDto {

    /** 攻撃シーケンス */
    @Tag(1)
    public List<BattleAtackDto> atacks;
    /** 触接 [味方, 敵] */
    @Tag(2)
    public int[] touchPlane;
    /** 制空状態 */
    @Tag(3)
    public String seiku;
    /** stage1 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全] */
    @Tag(4)
    public int[] stage1;
    /** stage2 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全] */
    @Tag(5)
    public int[] stage2;
    /** 対空カットイン [発動艦0-, 種別] */
    @Tag(8)
    public int[] airFire;
    @Tag(9)
    public int[] airFireItems;
    /** 基地航空隊ID */
    @Tag(21)
    public int airBaseId;

    private static int[] readPlaneCount(JsonObject stage) {
        return new int[] {
                stage.getInt("api_f_lostcount"),
                stage.getInt("api_f_count"),
                stage.getInt("api_e_lostcount"),
                stage.getInt("api_e_count")
        };
    }
    private static int[] readPlaneCount(LinkedTreeMap stage) {
        return new int[] {
                GsonUtil.toInt(stage.get("api_f_lostcount")),
                GsonUtil.toInt(stage.get("api_f_count")),
                GsonUtil.toInt(stage.get("api_e_lostcount")),
                GsonUtil.toInt(stage.get("api_e_count"))
        };
    }

    public AirBattleDto(int baseidx, JsonObject kouku, boolean isCombined, boolean isBase) {
        JsonValue jsonStage1 = kouku.get("api_stage1");
        if ((jsonStage1 != null) && (jsonStage1 != JsonValue.NULL)) {
            JsonObject jsonStage1Obj = kouku.getJsonObject("api_stage1");
            this.stage1 = readPlaneCount(jsonStage1Obj);
            if(jsonStage1Obj.containsKey("api_touch_plane")){
                JsonArray jsonTouchPlane = jsonStage1Obj.getJsonArray("api_touch_plane");
                this.touchPlane = new int[] {
                        jsonTouchPlane.getInt(0),
                        jsonTouchPlane.getInt(1)
                };
            }else{
                this.touchPlane = new int[]{-1,-1};
            }
            if(jsonStage1Obj.containsKey("api_disp_seiku")){
                this.seiku = toSeiku(jsonStage1Obj.getInt("api_disp_seiku"));
            }
            else{
                this.seiku = toSeiku(-1);
            }
        }

        JsonObject jsonStage2 = JsonUtils.getJsonObject(kouku, "api_stage2");
        if (jsonStage2 != null) {
            this.stage2 = readPlaneCount(jsonStage2);

            JsonObject jsonAirFire = JsonUtils.getJsonObject(jsonStage2, "api_air_fire");
            if (jsonAirFire != null) {
                this.airFire = new int[] {
                        jsonAirFire.getInt("api_idx"),
                        jsonAirFire.getInt("api_kind")
                };
                this.airFireItems = JsonUtils.getIntArray(jsonAirFire, "api_use_items");
            }
        }

        this.atacks = BattleAtackDto.makeAir(
                baseidx, 6,
                JsonUtils.getJsonArray(kouku, "api_plane_from"),
                JsonUtils.getJsonObject(kouku, "api_stage3"),
                isCombined ? JsonUtils.getJsonObject(kouku, "api_stage3_combined") : null,
                isBase);
    }

    public AirBattleDto(LinkedTreeMap kouku, boolean isCombined, boolean isBase,boolean splitHp) {
        LinkedTreeMap jsonStage1 = (LinkedTreeMap)kouku.get("api_stage1");
        if (jsonStage1 != null) {
            this.stage1 = readPlaneCount(jsonStage1);
            int[] jsonTouchPlane = GsonUtil.toIntArray(jsonStage1.get("api_touch_plane"));
            this.touchPlane = (jsonTouchPlane != null) ?jsonTouchPlane :(new int[]{-1,-1});
            this.seiku = toSeiku(GsonUtil.toInt(jsonStage1.get("api_disp_seiku")));
        }

        LinkedTreeMap jsonStage2 = (LinkedTreeMap)kouku.get("api_stage2");
        if (jsonStage2 != null) {
            this.stage2 = readPlaneCount(jsonStage2);
            LinkedTreeMap jsonAirFire = (LinkedTreeMap)jsonStage2.get("api_air_fire");
            if (jsonAirFire != null) {
                this.airFire = new int[] {
                        GsonUtil.toInt(jsonAirFire.get("api_idx")),
                        GsonUtil.toInt(jsonAirFire.get("api_kind"))
                };
                this.airFireItems = GsonUtil.toIntArray(jsonAirFire.get("api_use_items"));
            }
        }

        this.atacks = BattleAtackDto.makeAir(
                GsonUtil.toIntArrayArray(kouku.get("api_plane_from")),
                (LinkedTreeMap)kouku.get("api_stage3"),
                isCombined ? (LinkedTreeMap)kouku.get("api_stage3_combined") : null,
                isBase,
                splitHp);
    }

    private static String toSeiku(int id) {
        switch (id) {
        case 1:
            return "制空権確保";
        case 2:
            return "航空優勢";
        case 0:
            return "航空互角";
        case 3:
            return "航空劣勢";
        case 4:
            return "制空権喪失";
        default:
            return "不明(" + id + ")";
        }
    }

    private static String toTaikuCutin(int id) {
        switch (id) {
        case 1:
            return "高角砲x2/電探";
        case 2:
            return "高角砲/電探";
        case 3:
            return "高角砲x2";
        case 4:
            return "大口径主砲/三式弾/高射装置/電探";
        case 5:
            return "高角砲+高射装置x2/電探";
        case 6:
            return "大口径主砲/三式弾/高射装置";
        case 7:
            return "高角砲/高射装置/電探";
        case 8:
            return "高角砲+高射装置/電探";
        case 9:
            return "高角砲/高射装置";
        case 10:
            return "高角砲/集中機銃/電探";
        case 11:
            return "高角砲/集中機銃";
        case 12:
            return "集中機銃/機銃/電探";
        }
        return "不明(" + id + ")";
    }

    public String[] getStage1ShortString() {
        return getNumPlaneString(this.stage1, false);
    }

    public String[] getStage2ShortString() {
        return getNumPlaneString(this.stage2, false);
    }

    public String[] getStage1DetailedString() {
        return getNumPlaneString(this.stage1, true);
    }

    public String[] getStage2DetailedString() {
        return getNumPlaneString(this.stage2, true);
    }

    public String getTaikuCutinString() {
        return toTaikuCutin(this.airFire[1]);
    }

    public String getTaikuCutinItemsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.airFireItems.length; ++i) {
            ItemInfoDto item = Item.get(this.airFireItems[i]);
            if (i > 0) {
                sb.append("/");
                if (item != null) {
                    sb.append(item.getName());
                }
                else {
                    sb.append("装備不明");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 艦載機ロスト表示を生成 [味方・敵]
     * @param stage
     * @return
     */
    private static String[] getNumPlaneString(int[] stage, boolean detail) {
        if (stage == null) {
            return new String[] { "", "" };
        }
        int flost = stage[0];
        int fall = stage[1];
        int elost = stage[2];
        int eall = stage[3];
        int fremain = fall - flost;
        int eremain = eall - elost;
        if (detail) {
            return new String[] {
                    String.valueOf(fall) + "→" + fremain + " (-" + flost + ")",
                    String.valueOf(eall) + "→" + eremain + " (-" + elost + ")"
            };
        }
        else {
            return new String[] {
                    String.valueOf(fall) + "→" + fremain,
                    String.valueOf(eall) + "→" + eremain
            };
        }
    }

    public static String[] toTouchPlaneString(int[] touchPlane) {
        if (touchPlane == null) {
            return new String[] { "", "" };
        }
        String[] ret = new String[2];
        for (int i = 0; i < 2; ++i) {
            if (touchPlane[i] == -1) {
                ret[i] = "なし";
            }
            else {
                ItemInfoDto item = Item.get(touchPlane[i]);
                if (item != null) {
                    ret[i] = item.getName();
                }
                else {
                    ret[i] = "あり（機体不明）";
                }
            }
        }
        return ret;
    }

    /**
     * 触接表示を生成 [味方・敵]
     * @param touchPlane
     * @return
     */
    public String[] getTouchPlane() {
        return toTouchPlaneString(this.touchPlane);
    }

    /**
     * 基地艦載機表示を生成
     * @return String
     */
    public String getBasePlane(int mst_id) {
        ItemInfoDto item = Item.get(mst_id);
        if(item != null){
            return item.getName();
        }else{
            return null;
        }
    }
}
