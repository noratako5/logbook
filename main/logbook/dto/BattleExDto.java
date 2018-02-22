/**
 *
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.IntStream;
import java.util.Calendar;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;

import com.dyuproject.protostuff.Tag;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import logbook.data.DataType;
import logbook.data.context.GlobalContext;
import logbook.internal.EnemyData;
import logbook.internal.LoggerHolder;
import logbook.internal.UseItem;
import logbook.util.GsonUtil;
import logbook.util.JsonUtils;

/**
 * １回の会敵情報
 * @author Nekopanda
 */
public class BattleExDto extends AbstractDto {
    private static LoggerHolder LOG = new LoggerHolder("builtinScript");
    private static Date enemyIDUpdatedDate = null;
    private static Date getEnemyIDUpdatedDate() {
        if(enemyIDUpdatedDate == null) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("JST"));
            c.clear();
            c.set(2017, 4 - 1, 5, 12, 00, 00);
            enemyIDUpdatedDate = c.getTime();
        }
        return  enemyIDUpdatedDate;
    }
    private static Date flarePosUpdatedDate = null;
    private static Date getFlarePosUpdatedDate(){
        if(flarePosUpdatedDate == null) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("JST"));
            c.clear();
            c.set(2018, 2 - 1, 17, 0, 00, 00);
            flarePosUpdatedDate = c.getTime();
        }
        return  flarePosUpdatedDate;
    }


    /** 日付 */
    @Tag(1)
    private final Date battleDate;

    /** 味方艦隊 */
    @Tag(2)
    private final List<DockDto> friends = new ArrayList<>();

    /** 敵艦隊 */
    @Tag(3)
    private final List<EnemyShipDto> enemy = new ArrayList<>();

    /** 敵随伴艦隊 */
    @Tag(31)
    private List<EnemyShipDto> enemyCombined = new ArrayList<>();

    /** 味方MaxHP */
    @Tag(6)
    private int[] maxFriendHp;

    @Tag(7)
    private int[] maxFriendHpCombined;

    /** 敵MaxHP */
    @Tag(8)
    private int[] maxEnemyHp;

    @Tag(32)
    private int[] maxEnemyHpCombined;

    /** 味方戦闘開始時HP */
    @Tag(9)
    private int[] startFriendHp;

    @Tag(10)
    private int[] startFriendHpCombined;

    /** 敵戦闘開始時HP */
    @Tag(11)
    private int[] startEnemyHp;

    @Tag(33)
    private int[] startEnemyHpCombined;

    /** 戦闘前の味方総HP */
    @Tag(12)
    private int friendGaugeMax = 0;

    /** 戦闘前の敵総HP */
    @Tag(13)
    private int enemyGaugeMax = 0;

    /** 陣形（味方・敵） */
    @Tag(14)
    private final String[] formation = new String[] { "陣形不明", "陣形不明" };

    /** 同航戦とか　*/
    @Tag(15)
    private String formationMatch = "不明";

    /** 索敵状態（味方・敵） */
    @Tag(16)
    private String sakuteki[];

    /** 攻撃フェーズ */
    @Tag(17)
    private final List<Phase> phaseList = new ArrayList<Phase>();

    /** 海域名 */
    @Tag(18)
    private String questName;

    /** ランク */
    @Tag(19)
    private ResultRank rank;

    /** マス */
    @Tag(20)
    private MapCellDto mapCellDto;

    /** 敵艦隊名 */
    @Tag(21)
    private String enemyName;

    /** ドロップフラグ */
    @Tag(22)
    private boolean dropShip;

    /** ドロップフラグ */
    @Tag(39)
    private boolean dropItem;

    /** 艦種 */
    @Tag(23)
    private String dropType;

    /** 艦名 */
    @Tag(24)
    private String dropName;

    /** ドロップ艦ID */
    @Tag(46)
    private int dropShipId;

    /** アイテム名 */
    @Tag(47)
    private String dropItemName;

    /** MVP艦（ゼロ始まりのインデックス） */
    @Tag(25)
    private int mvp;

    @Tag(26)
    private int mvpCombined;

    /** 提督Lv */
    @Tag(27)
    private int hqLv;

    /**
     * BattleExDtoのバージョン
     * exVersion == 0 : Tag 34以降がない
     * exVersion == 1 : Tag 36まである
     * exVersion == 2 : Jsonがある
     *  */
    @Tag(34)
    private int exVersion = 2;

    /** 母港空き（ドロップ分を含まない） */
    @Tag(35)
    private int shipSpace;

    /** 装備空き（ドロップ分を含まない） */
    @Tag(36)
    private int itemSpace;

    /** 連合艦隊における退避意見 [退避する艦(0-11), 護衛艦(0-11)] */
    @Tag(37)
    private int[] escapeInfo;

    @Tag(38)
    private boolean[] escaped;

    /** 轟沈フラグ */
    @Tag(44)
    private boolean[] lostflag;

    /** 次の連合艦隊ではこうなりそう(ならなかったら轟沈艦を取り除くことができないので処理を再考する必要あり) */
    //@Tag(45)
    //private boolean[] lostflagCombined;

    @Tag(51)
    private String resultJson;

    /** 連合艦隊の種類 */
    @Tag(52)
    private int combinedKind = 0;

    /** 新旧API */
    @Tag(91)
    private int baseidx = 0;

    @Tag(92)
    private int secondBase = 0;
    
    /////////////////////////////////////////////////

    /**
     * 戦闘1フェーズの情報
     * @author Nekopanda
     */
    public static class Phase {
        @Tag(1)
        private final BattlePhaseKind kind;
        /** 味方HP */
        @Tag(2)
        private final int[] nowFriendHp;

        @Tag(3)
        private final int[] nowFriendHpCombined;

        /** 敵HP */
        @Tag(4)
        private final int[] nowEnemyHp;

        @Tag(21)
        private final int[] nowEnemyHpCombined;

        /** ランク */
        @Tag(5)
        private ResultRank estimatedRank;

        /** 夜戦 */
        @Tag(6)
        private final boolean isNight;

        /** 敵は連合艦隊第二艦隊か？ */
        @Tag(22)
        private final boolean isEnemySecond;

        /** 自分は連合艦隊の第二艦隊か？*/
        @Tag(100)
        private final boolean isFriendSecond;

        /** 支援攻撃のタイプ */
        @Tag(7)
        private String supportType;

        /** 触接機（味方・敵） -1の場合は「触接なし」 */
        @Tag(8)
        private int[] touchPlane;

        /** 照明弾発射艦 */
        @Tag(32)
        private int[] flarePos;

        @Tag(9)
        private String seiku;

        /** 損害率（味方・敵） */
        @Tag(10)
        private double[] damageRate;

        /** 攻撃シーケンス */
        @Tag(36)
        private AirBattleDto airBaseInjection = null;
        @Tag(37)
        private AirBattleDto airInjection = null;
        @Tag(35)
        private List<AirBattleDto> airBase = null;
        @Tag(11)
        private AirBattleDto air = null;
        @Tag(12)
        private AirBattleDto air2 = null;
        @Tag(13)
        private List<BattleAtackDto> support = null;
        @Tag(50)
        private List<BattleAtackDto> openingTaisen = null;
        @Tag(14)
        private List<BattleAtackDto> opening = null;
        @Tag(15)
        private List<BattleAtackDto> raigeki = null;
        @Tag(16)
        private List<BattleAtackDto> hougeki = null;
        @Tag(17)
        private List<BattleAtackDto> hougeki1 = null;
        @Tag(18)
        private List<BattleAtackDto> hougeki2 = null;
        @Tag(19)
        private List<BattleAtackDto> hougeki3 = null;

        @Tag(30)
        private final String json;

        //json1回読んだらとりあえずここに詰めとく
        transient LinkedTreeMap treeCacheOrNull;
        transient List<BattleAtackDto> friendlyHougeki = null;
        //マスタデータは後から書き換わることがあるので毎回生成するのはアウト　タグ番号決めて保存する必要があるので本家対応待ち又は赤仮ログでゴリ押しのいずれか
        transient List<EnemyShipDto> friendlyShips = null;
        transient int[] nowFriendlyHp = null;
        transient int[] maxFriendlyHp = null;
        transient int[] friendlyFlarePos;
        transient int[] friendlyTouchPlane;
        public LinkedTreeMap getTree(){
            if(this.json != null && treeCacheOrNull == null){
                treeCacheOrNull = getGson().fromJson(this.json,LinkedTreeMap.class);
            }
            return treeCacheOrNull;
        }
        public void setTree(LinkedTreeMap tree){
            this.treeCacheOrNull = tree;
        }

        public Phase(BattleExDto battle, LinkedTreeMap tree,String json, BattlePhaseKind kind,
                int[] beforeFriendHp, int[] beforeFriendHpCombined, int[] beforeEnemyHp, int[] beforeEnemyHpCombined)
        {
            boolean isFriendCombined = (beforeFriendHpCombined != null);
            boolean isEnemyCombined = (beforeEnemyHpCombined != null);

            // 敵は連合艦隊の第二艦隊か？（敵連合艦隊夜戦で第二艦隊が相手の場合のみ）
            this.isEnemySecond = (tree.containsKey("api_active_deck")) ?(GsonUtil.toIntArray(tree.get("api_active_deck"))[1] == 2) : false;
            this.isFriendSecond = (tree.containsKey("api_active_deck")
                    ? (GsonUtil.toIntArray(tree.get("api_active_deck"))[0] == 2) : false);

            this.kind = kind;
            this.isNight = kind.isNight();

            this.nowFriendHp = beforeFriendHp.clone();
            this.nowEnemyHp = beforeEnemyHp.clone();
            this.nowFriendHpCombined = isFriendCombined ? beforeFriendHpCombined.clone() : null;
            this.nowEnemyHpCombined = isEnemyCombined ? beforeEnemyHpCombined.clone() : null;

            boolean splitHp = tree.containsKey("api_f_nowhps");

            //友軍艦隊フェーズ
            if(tree.containsKey(("api_friendly_battle"))) {
                this.friendlyShips = new ArrayList<>();
                LinkedTreeMap friendInfo  = (LinkedTreeMap) tree.get("api_friendly_info");
                int[] shipIds = GsonUtil.toIntArray(friendInfo.get("api_ship_id"));
                int[][] slots = GsonUtil.toIntArrayArray(friendInfo.get("api_Slot"));
                int[][] params = GsonUtil.toIntArrayArray(friendInfo.get("api_Param"));
                int[] levels = GsonUtil.toIntArray((friendInfo.get("api_ship_lv")));
                for (int i = 0; i < shipIds.length; i++) {
                    int id = shipIds[i];
                    int[] slot = slots[i];
                    int[] param = params[i];
                    this.friendlyShips.add(new EnemyShipDto(id, slot, param, levels[i]));
                }
                LinkedTreeMap friendBattle = (LinkedTreeMap) tree.get("api_friendly_battle");
                this.friendlyHougeki = BattleAtackDto.makeHougeki((LinkedTreeMap)friendBattle.get("api_hougeki"),false,this.isEnemySecond,splitHp);
                this.nowFriendlyHp = GsonUtil.toIntArray(friendInfo.get("api_nowhps"));
                this.maxFriendlyHp = GsonUtil.toIntArray(friendInfo.get("api_maxhps"));

                int[] jsonTouchPlane = GsonUtil.toIntArray(friendBattle.get("api_touch_plane"));
                this.friendlyTouchPlane = jsonTouchPlane;

                // 照明弾発射艦
                int[] jsonFlarePos = GsonUtil.toIntArray(friendBattle.get("api_flare_pos"));
                if(jsonFlarePos!=null && battle.getBattleDate().after(BattleExDto.getFlarePosUpdatedDate())){
                    for(int i=0;i<jsonFlarePos.length;i++){
                        if(jsonFlarePos[i] > 0) {
                            jsonFlarePos[i] += 1;
                        }
                    }
                }
                this.friendlyFlarePos = jsonFlarePos;
            }
            // 攻撃シーケンスを読み取る //
            if(kind == BattlePhaseKind.COMBINED_EC_NIGHT_TO_DAY_NIGHT){
                // 夜間触接
                int[] jsonTouchPlane = GsonUtil.toIntArray(tree.get("api_touch_plane"));
                this.touchPlane = jsonTouchPlane;

                // 照明弾発射艦
                int[] jsonFlarePos = GsonUtil.toIntArray(tree.get("api_flare_pos"));
                if(jsonFlarePos!=null && battle.getBattleDate().after(BattleExDto.getFlarePosUpdatedDate())){
                    for(int i=0;i<jsonFlarePos.length;i++){
                        if(jsonFlarePos[i] > 0) {
                            jsonFlarePos[i] += 1;
                        }
                    }
                }
                this.flarePos = jsonFlarePos;

                // 支援艦隊
                int support_flag = GsonUtil.toInt(tree.get("api_n_support_flag"));
                if (support_flag > 0) {
                    LinkedTreeMap support = (LinkedTreeMap) tree.get("api_n_support_info");
                    LinkedTreeMap support_hourai = (LinkedTreeMap) support.get("api_support_hourai");
                    LinkedTreeMap support_air = (LinkedTreeMap) support.get("api_support_airatack");
                    if (support_hourai != null) {
                        int[] edam = GsonUtil.toIntArray(support_hourai.get("api_damage"));
                        int[] ecl = GsonUtil.toIntArray(support_hourai.get("api_cl_list"));
                        if (edam != null) {
                            this.support = BattleAtackDto.makeSupport(edam, ecl, splitHp);
                        }
                    } else if (support_air != null) {
                        LinkedTreeMap stage3 = (LinkedTreeMap) support_air.get("api_stage3");
                        if (stage3 != null) {
                            this.support = BattleAtackDto.makeSupportAir(GsonUtil.toIntArray(stage3.get("api_edam")), GsonUtil.toIntArray(stage3.get("api_ecl_flag")), splitHp);
                        }
                    }
                    this.supportType = toSupport(support_flag);
                } else {
                    this.supportType = "";
                }

                // 砲撃
                this.hougeki1 = BattleAtackDto.makeHougeki((LinkedTreeMap) tree.get("api_n_hougeki1"), false,
                        this.isEnemySecond, splitHp);
                this.hougeki2 = BattleAtackDto.makeHougeki((LinkedTreeMap) tree.get("api_n_hougeki2"), false,
                        this.isEnemySecond, splitHp);
            }
            else {
                if(kind != BattlePhaseKind.COMBINED_EC_NIGHT_TO_DAY_DAY) {
                    // 夜間触接
                    int[] jsonTouchPlane = GsonUtil.toIntArray(tree.get("api_touch_plane"));
                    this.touchPlane = jsonTouchPlane;

                    // 照明弾発射艦
                    int[] jsonFlarePos = GsonUtil.toIntArray(tree.get("api_flare_pos"));
                    if(jsonFlarePos!=null && battle.getBattleDate().after(BattleExDto.getFlarePosUpdatedDate())){
                        for(int i=0;i<jsonFlarePos.length;i++){
                            if(jsonFlarePos[i] > 0) {
                                jsonFlarePos[i] += 1;
                            }
                        }
                    }
                    this.flarePos = jsonFlarePos;
                }

                LinkedTreeMap air_base_injection = (LinkedTreeMap) tree.get("api_air_base_injection");
                if (air_base_injection != null) {
                    this.airBaseInjection = new AirBattleDto(air_base_injection, isFriendCombined || isEnemyCombined, true, splitHp);
                }
                LinkedTreeMap air_injection_kouku = (LinkedTreeMap) tree.get("api_injection_kouku");
                if (air_injection_kouku != null) {
                    this.airInjection = new AirBattleDto(air_injection_kouku, isFriendCombined || isEnemyCombined, false, splitHp);
                }
                // 基地航空隊
                Object air_base_attack = tree.get("api_air_base_attack");
                if (air_base_attack instanceof List) {
                    this.airBase = new ArrayList<>();
                    for (Object item : (List) air_base_attack) {
                        this.airBase.add(new AirBattleDto((LinkedTreeMap) item, isFriendCombined || isEnemyCombined, true, splitHp));
                    }
                }

                // 航空戦（通常）
                LinkedTreeMap kouku = (LinkedTreeMap) tree.get("api_kouku");
                if (kouku != null) {
                    this.air = new AirBattleDto(kouku, isFriendCombined || isEnemyCombined, false, splitHp);
                    // 昼戦の触接はここ
                    this.touchPlane = this.air.touchPlane;
                    // 制空はここから取る
                    this.seiku = this.air.seiku;
                }

                // 支援艦隊
                int support_flag = GsonUtil.toInt(tree.get("api_support_flag"));
                if (support_flag <= 0 && kind != BattlePhaseKind.COMBINED_EC_NIGHT_TO_DAY_DAY) {
                    support_flag = GsonUtil.toInt(tree.get("api_n_support_flag"));
                }
                if (support_flag > 0) {
                    LinkedTreeMap support = null;
                    if (tree.containsKey("api_support_info")) {
                        support = (LinkedTreeMap) tree.get("api_support_info");
                    } else {
                        support = (LinkedTreeMap) tree.get("api_n_support_info");
                    }
                    LinkedTreeMap support_hourai = (LinkedTreeMap) support.get("api_support_hourai");
                    LinkedTreeMap support_air = (LinkedTreeMap) support.get("api_support_airatack");
                    if (support_hourai != null) {
                        int[] edam = GsonUtil.toIntArray(support_hourai.get("api_damage"));
                        int[] ecl = GsonUtil.toIntArray(support_hourai.get("api_cl_list"));
                        if (edam != null) {
                            this.support = BattleAtackDto.makeSupport(edam, ecl, splitHp);
                        }
                    } else if (support_air != null) {
                        LinkedTreeMap stage3 = (LinkedTreeMap) support_air.get("api_stage3");
                        if (stage3 != null) {
                            this.support = BattleAtackDto.makeSupportAir(GsonUtil.toIntArray(stage3.get("api_edam")), GsonUtil.toIntArray(stage3.get("api_ecl_flag")), splitHp);
                        }
                    }
                    this.supportType = toSupport(support_flag);
                } else {
                    this.supportType = "";
                }

                //航空戦
                LinkedTreeMap kouku2 = (LinkedTreeMap) tree.get("api_kouku2");
                if (kouku2 != null) {
                    this.air2 = new AirBattleDto(kouku2, isFriendCombined || isEnemyCombined, false, splitHp);
                }

                // 開幕対潜
                this.openingTaisen = BattleAtackDto.makeHougeki((LinkedTreeMap) tree.get("api_opening_taisen"), kind.isOpeningSecond(),
                        this.isEnemySecond, splitHp);

                // 開幕
                this.opening = BattleAtackDto.makeRaigeki((LinkedTreeMap) tree.get("api_opening_atack"), kind.isOpeningSecond(), splitHp);

                // 砲撃
                this.hougeki = BattleAtackDto.makeHougeki((LinkedTreeMap) tree.get("api_hougeki"), isCombined(),
                        this.isEnemySecond, splitHp); // 夜戦
                this.hougeki1 = BattleAtackDto.makeHougeki((LinkedTreeMap) tree.get("api_hougeki1"), kind.isHougeki1Second(),
                        this.isEnemySecond, splitHp);
                this.hougeki2 = BattleAtackDto.makeHougeki((LinkedTreeMap) tree.get("api_hougeki2"), kind.isHougeki2Second(),
                        this.isEnemySecond, splitHp);
                this.hougeki3 = BattleAtackDto.makeHougeki((LinkedTreeMap) tree.get("api_hougeki3"), kind.isHougeki3Second(),
                        this.isEnemySecond, splitHp);

                // 雷撃
                this.raigeki = BattleAtackDto.makeRaigeki((LinkedTreeMap) tree.get("api_raigeki"), kind.isRaigekiSecond(), splitHp);
            }

            // ダメージを反映 //
            this.doAtack(this.friendlyHougeki,battle.secondBase);
            if (this.airBaseInjection != null){
                this.doAtack(this.airBaseInjection.atacks, battle.secondBase);
            }
            if (this.airInjection != null){
                this.doAtack(this.airInjection.atacks, battle.secondBase);
            }
            if (this.airBase != null)
                for (AirBattleDto attack : this.airBase)
                    this.doAtack(attack.atacks, battle.secondBase);
            if (this.air != null)
                this.doAtack(this.air.atacks, battle.secondBase);
            this.doAtack(this.support, battle.secondBase);
            if (this.air2 != null)
                this.doAtack(this.air2.atacks, battle.secondBase);
            this.doAtack(this.openingTaisen, battle.secondBase);
            this.doAtack(this.opening, battle.secondBase);
            this.doAtack(this.hougeki, battle.secondBase);
            this.doAtack(this.hougeki1, battle.secondBase);
            this.doAtack(this.raigeki, battle.secondBase);
            this.doAtack(this.hougeki2, battle.secondBase);
            this.doAtack(this.hougeki3, battle.secondBase);

            this.json = json;
        }


        public void battleDamage(BattleExDto battle) {
            int numFships = this.nowFriendHp.length;
            int numEships = this.nowEnemyHp.length;
            DockDto dock = battle.getDock();
            DockDto dockCombined = battle.getDockCombined();

            // HP0以下を0にする
            for (int i = 0; i < numFships; i++) {
                this.nextHp(i, this.nowFriendHp, (dock != null) ? dock.getShips() : null);
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] <= 0)
                    this.nowEnemyHp[i] = 0;
            }
            if (this.nowFriendHpCombined != null) {
                for (int i = 0; i < this.nowFriendHpCombined.length; i++) {
                    this.nextHp(i, this.nowFriendHpCombined, (dockCombined != null) ? dockCombined.getShips() : null);
                }
            }
            if (this.nowEnemyHpCombined != null) {
                for (int i = 0; i < this.nowEnemyHpCombined.length; i++) {
                    if (this.nowEnemyHpCombined[i] <= 0)
                        this.nowEnemyHpCombined[i] = 0;
                }
            }

            // 判定を計算
            this.estimatedRank = this.calcResultRank(battle);
        }

        public void practiceDamage(BattleExDto battle) {
            int numFships = this.nowFriendHp.length;
            int numEships = this.nowEnemyHp.length;
            boolean isCombined = (this.nowFriendHpCombined != null);
            int numFshipsCombined = isCombined ? this.nowFriendHpCombined.length : 0;

            // HP0以下を0にする
            for (int i = 0; i < numFships; i++) {
                if (this.nowFriendHp[i] <= 0)
                    this.nowFriendHp[i] = 0;
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] <= 0)
                    this.nowEnemyHp[i] = 0;
            }
            if (isCombined) {
                for (int i = 0; i < numFshipsCombined; i++) {
                    if (this.nowFriendHpCombined[i] <= 0)
                        this.nowFriendHpCombined[i] = 0;
                }
            }

            // 判定を計算
            this.estimatedRank = this.calcResultRank(battle);
        }

        private void nextHp(int index, int[] hps, List<ShipDto> ships) {
            int hp = hps[index];
            if (ships == null) {
                if (hp <= 0) {
                    hps[index] = 0;
                }
                return;
            }
            else if(index >= ships.size()){
                return;
            }
            ShipDto ship = ships.get(index);
            if (hp <= 0) {
                List<ItemDto> items = new ArrayList<>(ship.getItem2());
                items.add(ship.getSlotExItem());
                for (ItemDto item : items) {
                    if (item == null)
                        continue;
                    if (item.getSlotitemId() == 42) { //応急修理要員
                        hps[index] = (int) (ship.getMaxhp() * 0.2);
                        return;
                    }
                    else if (item.getSlotitemId() == 43) { //応急修理女神
                        hps[index] = ship.getMaxhp();
                        return;
                    }
                }
                hps[index] = 0;
                return;
            }
            return;
        }

        // 勝利判定 //
        private ResultRank calcResultRank(BattleExDto battle) {
            boolean isFriendCombined = (this.nowFriendHpCombined != null);
            boolean isEnemyCombined = (this.nowEnemyHpCombined != null);
            int numFships = this.nowFriendHp.length;
            int numFshipsCombined = isFriendCombined ? this.nowFriendHpCombined.length : 0;
            int numEships = this.nowEnemyHp.length;
            int numEshipsCombined = isEnemyCombined ? this.nowEnemyHpCombined.length : 0;
            int[] nowFriendHp = this.nowFriendHp;
            int[] nowFriendHpCombined = this.nowFriendHpCombined;
            int[] nowEnemyHp = this.nowEnemyHp;
            int[] nowEnemyHpCombined = this.nowEnemyHpCombined;
            int friendEscaped = 0;

            // 自艦隊の戦闘終了時のHPの合計(A)
            int friendGauge = IntStream.range(0, numFships)
                    .filter(i -> !((battle.escaped != null) && battle.escaped[i]))
                    .map(i -> nowFriendHp[i]).sum();

            // 自艦隊の戦闘開始時のHPの合計(B)
            int friendGaugeMax = battle.friendGaugeMax;

            // 自艦隊の戦闘終了後の生存数
            int friendNowShips = (int) Arrays.stream(nowFriendHp).filter(hp -> hp > 0).count();

            // 連合艦隊(自艦隊)
            if (isFriendCombined) {
                friendGauge += IntStream.range(0, numFshipsCombined)
                        .filter(i -> !((battle.escaped != null) && battle.escaped[i + 6]))
                        .map(i -> nowFriendHpCombined[i]).sum();
                friendNowShips += (int) Arrays.stream(nowFriendHpCombined).filter(hp -> hp > 0).count();
                friendEscaped = (int) (battle.escaped != null ? IntStream.range(0, battle.escaped.length)
                        .mapToObj(i -> battle.escaped[i]).filter(escaped -> escaped).count() : 0);
            }

            // 自艦隊の轟沈数(C) (生存艦には退避した艦も含まれていることに注意)
            int friendSunk = (numFships + numFshipsCombined) - friendNowShips;

            // 敵艦隊の戦闘終了時のHPの合計(D)
            int enemyGauge = Arrays.stream(nowEnemyHp).sum();

            // 敵艦隊の戦闘開始時のHPの合計(E)
            int enamyGaugeMax = battle.enemyGaugeMax;

            // 敵艦隊の戦闘終了後の生存数
            int enemyNowShips = (int) Arrays.stream(nowEnemyHp).filter(hp -> hp > 0).count();

            // 連合艦隊(敵艦隊)
            if (isEnemyCombined) {
                enemyGauge += Arrays.stream(nowEnemyHpCombined).sum();
                enemyNowShips += (int) Arrays.stream(nowEnemyHpCombined).filter(hp -> hp > 0).count();
            }

            // 敵艦隊の轟沈数(F)
            int enemySunk = (numEships + numEshipsCombined) - enemyNowShips;

            // 自艦隊の戦闘開始時の艦船数(G)
            int numStartFships = (numFships + numFshipsCombined) - friendEscaped;

            // 敵艦隊の戦闘開始時の艦船数(H)
            int numStartEships = numEships + numEshipsCombined;

            this.damageRate = new double[] {
                    (double) (friendGaugeMax - friendGauge) / friendGaugeMax,
                    (double) (enamyGaugeMax - enemyGauge) / enamyGaugeMax,
            };

            double friendGaugeRate = Math.floor(this.damageRate[0] * 100);
            double enemyGaugeRate = Math.floor(this.damageRate[1] * 100);

            if ((this.kind == BattlePhaseKind.LD_AIRBATTLE) ||
                    (this.kind == BattlePhaseKind.COMBINED_LD_AIR)) {
                // 空襲戦
                // S勝利は発生しないと思われる(完全勝利Sのみ)
                if (friendGaugeMax <= friendGauge) {
                    return ResultRank.PERFECT;
                }
                if (friendGaugeRate < 10) {
                    return ResultRank.A;
                }
                if (friendGaugeRate < 20) {
                    return ResultRank.B;
                }
                if (friendGaugeRate < 50) {
                    return ResultRank.C;
                }
                if (friendGaugeRate < 80) {
                    return ResultRank.D;
                }
                return ResultRank.E;
            }
            else {
                // PHASE1:轟沈艦なし かつ 敵艦全滅
                if ((friendSunk == 0) && (enemySunk == numStartEships)) {
                    // 戦闘終了時のHPが戦闘開始時のHP以上の場合、完全勝利S判定にする
                    // ソースはscenes/BattleResultMain.swfのgetTweenShowRank()参照
                    if (friendGaugeMax <= friendGauge) {
                        return ResultRank.PERFECT;
                    }
                    else {
                        return ResultRank.S;
                    }
                }
                // PHASE2:轟沈艦なし かつ 敵艦隊の戦闘開始時の数が1隻より上 かつ 敵艦の撃沈数が7割以上
                else if ((friendSunk == 0) && (numStartEships > 1)
                        && (enemySunk >= Math.floor(0.7 * numStartEships))) {
                    return ResultRank.A;
                }
                // PHASE3:自艦隊の轟沈数より敵艦隊の撃沈数の方が多い かつ 敵旗艦撃沈
                else if ((friendSunk < enemySunk) && (nowEnemyHp[0] == 0)) {
                    return ResultRank.B;
                }
                // PHASE4:自艦隊が1隻のみ かつ 自旗艦大破
                else if ((numStartFships == 1) && (((double) nowFriendHp[0] / battle.getMaxFriendHp()[0]) <= 0.25)) {
                    return ResultRank.D;
                }
                // PHASE5:敵艦隊の損害率が自艦隊の損害率を2.5倍しても上なら
                else if (enemyGaugeRate > (2.5 * friendGaugeRate)) {
                    return ResultRank.B;
                }
                // PHASE6:敵艦隊の損害率が自艦隊の損害率を0.9倍しても上なら
                else if (enemyGaugeRate > (0.9 * friendGaugeRate)) {
                    return ResultRank.C;
                }
                // PHASE7:開始時2隻以上 かつ 旗艦以外全滅
                else if ((numStartFships > 1) && ((numStartFships - 1) == friendSunk)) {
                    return ResultRank.E;
                }
                // 残りはD
                else {
                    return ResultRank.D;
                }
            }
        }

        // ダメージを反映
        private void doAtack(List<BattleAtackDto> seq, int secondBase) {
            if (seq == null)
                return;

            for (BattleAtackDto dto : seq) {
                for (int i = 0; i < dto.target.length; ++i) {
                    int target = dto.target[i];
                    int damage = dto.damage[i];
                    if(damage == 0){
                        continue;
                    }
                    if (dto.friendAtack) {
                        if (target < this.nowEnemyHp.length) {
                            this.nowEnemyHp[target] -= damage;
                        }
                        else {
                            this.nowEnemyHpCombined[target - 6] -= damage;
                        }
                    }
                    else {
                        if (target < this.nowFriendHp.length) {
                            this.nowFriendHp[target] -= damage;
                        }
                        else {
                            this.nowFriendHpCombined[target - secondBase] -= damage;
                        }
                    }
                }
            }
        }

        /**
         * 連合艦隊か？
         * @return
         */
        public boolean isCombined() {
            return (this.nowFriendHpCombined != null);
        }

        /**
         * 航空戦情報 [１回目, 2回目]
         * 2回目は連合艦隊航空戦マスでの戦闘のみ
         * @return
         */
        public AirBattleDto[] getAirBattleDto() {
            return new AirBattleDto[] {
                    this.air, this.air2
            };
        }

        private BattleAtackDto[] toArray(List<BattleAtackDto> list) {
            return list.toArray(new BattleAtackDto[list.size()]);
        }

        private BattleAtackDto[] getAirBaseBattlesArray() {
            if (this.airBase == null) {
                return null;
            }
            List<BattleAtackDto> arr = new ArrayList<>();
            for (AirBattleDto dto : this.airBase) {
                if (dto.atacks != null) {
                    arr.addAll(dto.atacks);
                }
            }
            return arr.toArray(new BattleAtackDto[0]);
        }

        /**
         * 攻撃の全シーケンスを取得
         * [ 噴式基地航空隊航空戦, 噴式航空戦, 基地航空隊航空戦, 航空戦1, 支援艦隊の攻撃, 航空戦2, 開幕, 夜戦, 砲撃戦1, 雷撃, 砲撃戦2, 砲撃戦3 ]
         * 各戦闘がない場合はnullになる
         * @return
         */
        public BattleAtackDto[][] getAtackSequence() {
            return new BattleAtackDto[][] {
                    this.friendlyHougeki == null ? null : this.toArray(this.friendlyHougeki),
                    ((this.airBaseInjection == null) || (this.airBaseInjection.atacks == null)) ? null : this
                            .toArray(this.airBaseInjection.atacks),
                    ((this.airInjection == null) || (this.airInjection.atacks == null)) ? null : this
                            .toArray(this.airInjection.atacks),
                    this.getAirBaseBattlesArray(),
                    ((this.air == null) || (this.air.atacks == null)) ? null : this.toArray(this.air.atacks),
                    this.support == null ? null : this.toArray(this.support),
                    ((this.air2 == null) || (this.air2.atacks == null)) ? null : this.toArray(this.air2.atacks),
                    this.openingTaisen == null ? null : this.toArray(this.openingTaisen),
                    this.opening == null ? null : this.toArray(this.opening),
                    this.hougeki == null ? null : this.toArray(this.hougeki),
                    this.hougeki1 == null ? null : this.toArray(this.hougeki1),
                    this.raigeki == null ? null : this.toArray(this.raigeki),
                    this.hougeki2 == null ? null : this.toArray(this.hougeki2),
                    this.hougeki3 == null ? null : this.toArray(this.hougeki3),
            };
        }

        /**
         * 戦闘ランクの計算に使われた情報の概要を取得
         * @param battle
         * @return
         */
        public String getRankCalcInfo(BattleExDto battle) {
            boolean isCombined = (this.nowFriendHpCombined != null);
            int numFships = this.nowFriendHp.length;
            int numFshipsCombined = isCombined ? this.nowFriendHpCombined.length : 0;
            int numEships = this.nowEnemyHp.length;
            // 戦闘後に残っている艦数
            int friendNowShips = 0;
            int enemyNowShips = 0;
            // 総ダメージ
            int friendGauge = 0;
            int enemyGauge = 0;

            for (int i = 0; i < numFships; i++) {
                if (this.nowFriendHp[i] > 0) {
                    ++friendNowShips;
                }
                friendGauge += battle.getStartFriendHp()[i] - this.nowFriendHp[i];

            }
            if (isCombined) {
                for (int i = 0; i < numFshipsCombined; i++) {
                    if (this.nowFriendHpCombined[i] > 0) {
                        ++friendNowShips;
                    }
                    friendGauge += battle.getStartFriendHpCombined()[i] - this.nowFriendHpCombined[i];
                }
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] > 0)
                    ++enemyNowShips;

                enemyGauge += battle.getStartEnemyHp()[i] - this.nowEnemyHp[i];
            }

            //double enemyGaugeRate = (double) enemyGauge / this.enemyGaugeMax;
            //double friendGaugeRate = (double) friendGauge / this.friendGaugeMax;

            return "味方[艦:" + (numFships + numFshipsCombined) + "→" + friendNowShips + " ゲージ:" +
                    friendGauge + "/" + battle.getFriendGaugeMax() +
                    "] 敵[艦:" + this.nowEnemyHp.length + "→" + enemyNowShips + " ゲージ:" + enemyGauge + "/"
                    + battle.getEnemyGaugeMax() +
                    "]" +
                    //"(" + (enemyGaugeRate / friendGaugeRate) + "/" + (friendGaugeRate / enemyGaugeRate) + ") " +
                    "判定:" + this.estimatedRank.rank();
        }

        /**
         * 受け取ったJSON
         * @return
         */
        public JsonObject getJson() {
            if (this.json == null) {
                return null;
            }
            return JsonUtils.fromString(this.json);
        }

        /**
         * この戦闘フェーズのAPIリクエスト先
         * @return
         */
        public String getApi() {
            return this.kind.getApi().getApiName();
        }

        /**
         * この戦闘フェーズの種別
         * @return kind
         */
        public BattlePhaseKind getKind() {
            return this.kind;
        }

        /**
         * 処理対象の艦隊は連合艦隊の第二艦隊か？
         * @return isFriendSecond
         */
        public boolean getisFriendSecond() {
            return this.isFriendSecond;
        }

        /**
         * この戦闘フェーズ後の味方艦HP（連合艦隊の時は第一艦隊）
         * @return nowFriendHp
         */
        public int[] getNowFriendHp() {
            return this.nowFriendHp;
        }

        /**
         * この戦闘フェーズ後の味方艦HP（連合艦隊でないときはnull）
         * @return nowFriendHpCombined
         */
        public int[] getNowFriendHpCombined() {
            return this.nowFriendHpCombined;
        }

        /**
         * この戦闘フェーズ後の敵艦HP（敵連合艦隊の時は第一艦隊）
         * @return nowEnemyHp
         */
        public int[] getNowEnemyHp() {
            return this.nowEnemyHp;
        }

        /**
         * この戦闘フェーズ後の敵艦HP（敵連合艦隊でないときはnull）
         * @return nowEnemyHp
         */
        public int[] getNowEnemyHpCombined() {
            return this.nowEnemyHpCombined;
        }

        /**
         * この戦闘フェーズ後のランク（予測値）
         * @return estimatedRank
         */
        public ResultRank getEstimatedRank() {
            return this.estimatedRank;
        }

        /**
         * この戦闘フェーズが夜戦か？
         * @return isNight
         */
        public boolean isNight() {
            return this.isNight;
        }

        /**
         * 敵は連合艦隊第二艦隊か？
         * @return isEnemySecond
         */
        public boolean isEnemySecond() {
            return this.isEnemySecond;
        }

        /**
         * 支援攻撃のタイプ
         * @return supportType
         */
        public String getSupportType() {
            return this.supportType;
        }

        /**
         * 触接機 [味方・敵] -1の場合は「触接なし」
         * @return touchPlane
         */
        public int[] getTouchPlane() {
            return this.touchPlane;
        }

        /**
         * 制空状態
         * @return seiku
         */
        public String getSeiku() {
            return this.seiku;
        }

        /**
         * 損害率 [味方, 敵]
         * @return damageRate
         */
        public double[] getDamageRate() {
            return this.damageRate;
        }

        /**
         * @return airInjection
         */
        public AirBattleDto getAirInjection() {
            return this.airInjection;
        }

        /**
         * 航空戦1
         * @return air
         */
        public AirBattleDto getAir() {
            return this.air;
        }

        /**
         * 航空戦2
         * @return air2
         */
        public AirBattleDto getAir2() {
            return this.air2;
        }

        /**
         * 支援艦隊の攻撃
         * @return support
         */
        public List<BattleAtackDto> getSupport() {
            return this.support;
        }


        /**
         * 開幕対潜
         * @return openingTaisen
         */
        public List<BattleAtackDto> getOpeningTaisen() {
            return this.openingTaisen;
        }

        /**
         * 開幕
         * @return opening
         */
        public List<BattleAtackDto> getOpening() {
            return this.opening;
        }

        /**
         * 雷撃戦
         * @return raigeki
         */
        public List<BattleAtackDto> getRaigeki() {
            return this.raigeki;
        }

        /**
         * 夜戦
         * @return hougeki
         */
        public List<BattleAtackDto> getHougeki() {
            return this.hougeki;
        }

        /**
         * 砲撃戦1
         * @return hougeki1
         */
        public List<BattleAtackDto> getHougeki1() {
            return this.hougeki1;
        }

        /**
         * 砲撃戦2
         * @return hougeki2
         */
        public List<BattleAtackDto> getHougeki2() {
            return this.hougeki2;
        }

        /**
         * 砲撃戦3
         * @return hougeki3
         */
        public List<BattleAtackDto> getHougeki3() {
            return this.hougeki3;
        }

        /**
         * @param touchPlane セットする touchPlane
         */
        public void setTouchPlane(int[] touchPlane) {
            this.touchPlane = touchPlane;
        }

        /**
         * @return flarePos
         */
        public int[] getFlarePos() {
            return this.flarePos;
        }

        /**
         * @return airBaseInjection
         */
        public AirBattleDto getAirBaseInjection() {
            return this.airBaseInjection;
        }

        /**
         * @return airBase
         */
        public List<AirBattleDto> getAirBase() {
            return this.airBase;
        }

        public  List<BattleAtackDto> getFriendlyHougeki(){return this.friendlyHougeki;}

        public int[] getNowFriendlyHp(){return this.nowFriendlyHp;}
        public int[] getMaxFriendlyHp(){return this.maxFriendlyHp;}
        public  List<EnemyShipDto> getFriendlyShips(){return this.friendlyShips;}
        public int[] getFriendlyFlarePos(){return this.friendlyFlarePos;}
        public int[] getFriendlyTouchPlane(){return this.touchPlane;}
    }

    /**
     * 戦闘データオブジェクト作成
     * @param date 戦闘のあった日時
     */
    public BattleExDto(Date date) {
        this.battleDate = date;
    }

    /**
     * 母港情報を設定
     * @param shipSpace
     * @param itemSpace
     */
    public void setBasicInfo(int shipSpace, int itemSpace) {
        this.shipSpace = shipSpace;
        this.itemSpace = itemSpace;
    }

    /**
     * 中に保存してあるJSONを使ってフィールドを更新する
     */
    public void readFromJson() {
        // 後から追加したフィールドはnullになっているので最低限のオブジェクトを作成する
        if (this.enemyCombined == null)
            this.enemyCombined = new ArrayList<>();

        if (this.exVersion >= 2) {
            // Jsonが保存してあるのはバージョン2以降
            Phase[] phaseCopy = this.phaseList.toArray(new Phase[0]);
            this.enemy.clear();
            this.enemyCombined.clear();
            this.phaseList.clear();
            for (Phase phase : phaseCopy) {
                //this.addPhase(phase.getJson(), phase.getKind());
                String json = phase.json;
                LinkedTreeMap tree = getGson().fromJson(json,LinkedTreeMap.class);
                this.addPhase2(tree,json,phase.getKind());
            }
            //this.readResultJson(JsonUtils.fromString(this.resultJson));
            this.readResultJson2(getGson().fromJson(this.resultJson,LinkedTreeMap.class));
        }
        else {
            // 旧バージョンのログに対応
            // ドロップの"アイテム"をdropItemNameに移動させる
            if (this.dropItem && !this.dropShip && StringUtils.isEmpty(this.dropItemName)) {
                this.dropItemName = this.dropName;
                this.dropName = "";
                this.dropType = "";
            }
        }
    }

    /**
     * 戦闘フェーズ結果を読み込む
     * @param object 受け取ったJSON
     * @param kind 戦闘の種別
     * @return 作成されたPhaseオブジェクト
     */
    public Phase addPhase(JsonObject object, BattlePhaseKind kind) {
            String json = object.toString();
            LinkedTreeMap tree =  getGson().fromJson(json,LinkedTreeMap.class);
            return addPhase2(tree,json,kind);
    }

    /**
     * 戦闘フェーズ結果を読み込む
     * @param tree 受け取ったJSON
     * @param kind 戦闘の種別
     * @return 作成されたPhaseオブジェクト
     */
    public Phase addPhase2(LinkedTreeMap tree,String json,BattlePhaseKind kind) {
        if (this.phaseList.size() == 0) {
            // 最初のフェーズ
            String dockId;
            if (tree.containsKey("api_dock_id")) {
                dockId = GsonUtil.toIntString(tree.get("api_dock_id"));
            } else {
                dockId = GsonUtil.toIntString(tree.get("api_deck_id"));
            }

            int[] nowhps = null;
            if(tree.containsKey("api_nowhps")){
                nowhps = GsonUtil.toIntArray(tree.get("api_nowhps"));
            }
            int[] fNowHps = null;
            if(tree.containsKey("api_f_nowhps")){
                fNowHps = GsonUtil.toIntArray(tree.get("api_f_nowhps"));
            }
            int[] eNowHps = null;
            if(tree.containsKey("api_e_nowhps")){
                eNowHps = GsonUtil.toIntArray(tree.get("api_e_nowhps"));
            }
            int[] maxhps = null;
            if(tree.containsKey("api_maxhps")) {
                maxhps = GsonUtil.toIntArray(tree.get("api_maxhps"));
            }
            int[] fMaxHps = null;
            if(tree.containsKey("api_f_maxhps")){
                fMaxHps = GsonUtil.toIntArray(tree.get("api_f_maxhps"));
            }
            int[] eMaxHps = null;
            if(tree.containsKey("api_e_maxhps")){
                eMaxHps = GsonUtil.toIntArray(tree.get("api_e_maxhps"));
            }
            int[] fNowHpsCombined = null;
            if(tree.containsKey("api_f_nowhps_combined")){
                fNowHpsCombined = GsonUtil.toIntArray(tree.get("api_f_nowhps_combined"));
            }
            int[] fMaxHpsCombined = null;
            if(tree.containsKey("api_f_maxhps_combined")){
                fMaxHpsCombined = GsonUtil.toIntArray(tree.get("api_f_maxhps_combined"));
            }
            int[] eNowHpsCombined = null;
            if(tree.containsKey("api_e_nowhps_combined")){
                eNowHpsCombined = GsonUtil.toIntArray(tree.get("api_e_nowhps_combined"));
            }
            int[] eMaxHpsCombined = null;
            if(tree.containsKey("api_e_maxhps_combined")){
                eMaxHpsCombined = GsonUtil.toIntArray(tree.get("api_e_maxhps_combined"));
            }

            int[] nowhpsCombined = GsonUtil.toIntArray(tree.get("api_nowhps_combined"));
            int[] maxhpsCombined = GsonUtil.toIntArray(tree.get("api_maxhps_combined"));
            boolean isFriendCombined = tree.containsKey("api_fParam_combined");
            boolean isEnemyCombined = tree.containsKey("api_eParam_combined");

            int numFships = 6; // 旧API用初期値
            int numFshipsCombined = 0;

            this.secondBase = 6;

            if(nowhps != null) {
                for (int i = 1; i <= 6; ++i) {
                    if (maxhps[i] == -1) {
                        numFships = i - 1;
                        break;
                    }
                }
            }
            else{
                numFships = fNowHps.length;
            }

            if (tree.containsKey("api_fParam_combined")) {
                if(nowhps != null) {
                    numFshipsCombined = 6;
                    for (int i = 1; i <= 6; ++i) {
                        if (maxhpsCombined[i] == -1) {
                            numFshipsCombined = i - 1;
                            break;
                        }
                    }
                }
                else{
                    numFshipsCombined = fNowHpsCombined.length;
                }
            }

            // 第一艦隊が6隻より大きかったらsecondBaseも大きくする
            if (this.secondBase < numFships) {
                this.secondBase = numFships;
            }

            if (this.friends.size() == 0) { // 再読み込みの場合はスキップ
                this.friends.add(GlobalContext.getDock(dockId));
                if (numFshipsCombined > 0) {
                    this.friends.add(GlobalContext.getDock("2"));
                }
            }

            int[] shipKe = GsonUtil.toIntArray(tree.get("api_ship_ke"));
            int[][] eSlots = GsonUtil.toIntArrayArray(tree.get("api_eSlot"));
            int[][] eParams = GsonUtil.toIntArrayArray(tree.get("api_eParam"));
            int[] eLevel = GsonUtil.toIntArray(tree.get("api_ship_lv"));
            boolean isOldEnemyId = this.getBattleDate().before(getEnemyIDUpdatedDate());
            if(shipKe[0] == -1) {
                for (int i = 1; i < shipKe.length; i++) {
                    int id = shipKe[i];
                    if (id != -1) {
                        int[] slot = eSlots[i - 1];
                        int[] param = eParams[i - 1];
                        if (isOldEnemyId && id > 500) {
                            id += 1000;
                        }
                        this.enemy.add(new EnemyShipDto(id, slot, param, eLevel[i]));
                    }
                }
            }
            else{
                for (int i = 0; i < shipKe.length; i++) {
                    int id = shipKe[i];
                    if (id != -1) {
                        int[] slot = eSlots[i];
                        int[] param = eParams[i];
                        if (isOldEnemyId && id > 500) {
                            id += 1000;
                        }
                        this.enemy.add(new EnemyShipDto(id, slot, param, eLevel[i]));
                    }
                }
            }
            if (isEnemyCombined) {
                int[] shipKeCombined = GsonUtil.toIntArray(tree.get("api_ship_ke_combined"));
                int[][] eSlotsCombined = GsonUtil.toIntArrayArray(tree.get("api_eSlot_combined"));
                int[][] eParamsCombined = GsonUtil.toIntArrayArray(tree.get("api_eParam_combined"));
                int[] eLevelCombined = GsonUtil.toIntArray(tree.get("api_ship_lv_combined"));
                if(shipKeCombined[0] == -1){
                    for (int i = 1; i < shipKeCombined.length; i++) {
                        int id = shipKeCombined[i];
                        if (id != -1) {
                            int[] slot = eSlotsCombined[i - 1];
                            int[] param = eParamsCombined[i - 1];
                            if(isOldEnemyId && id > 500){
                                id += 1000;
                            }
                            this.enemyCombined.add(new EnemyShipDto(id, slot, param, eLevelCombined[i]));
                        }
                    }
                }
                else{
                    for (int i = 0; i < shipKeCombined.length; i++) {
                        int id = shipKeCombined[i];
                        if (id != -1) {
                            int[] slot = eSlotsCombined[i];
                            int[] param = eParamsCombined[i];
                            if(isOldEnemyId && id > 500){
                                id += 1000;
                            }
                            this.enemyCombined.add(new EnemyShipDto(id, slot, param, eLevelCombined[i]));
                        }
                    }
                }

            }
            int numEships = this.enemy.size();
            int numEshipsCombined = this.enemyCombined.size();

            this.startFriendHp = new int[numFships];
            this.startEnemyHp = new int[numEships];
            this.maxFriendHp = new int[numFships];
            this.maxEnemyHp = new int[numEships];
            if (isFriendCombined) {
                this.startFriendHpCombined = new int[numFshipsCombined];
                this.maxFriendHpCombined = new int[numFshipsCombined];
            }
            else {
                this.maxFriendHpCombined = null;
            }
            if (isEnemyCombined) {
                this.startEnemyHpCombined = new int[numEshipsCombined];
                this.maxEnemyHpCombined = new int[numEshipsCombined];
            }
            else {
                this.maxEnemyHpCombined = null;
            }

            // 陣形
            if (tree.containsKey("api_formation")) {
                int[] formation = GsonUtil.toIntArray(tree.get("api_formation"));
                for (int i = 0; i < 2; ++i) {
                    this.formation[i] = toFormation(formation[i]);
                }
                this.formationMatch = toMatch(formation[2]);
            }

            // 索敵
            int[] jsonSearch = GsonUtil.toIntArray(tree.get("api_search"));
            if (jsonSearch != null) {
                this.sakuteki = new String[] {
                        toSearch(jsonSearch[0]),
                        toSearch(jsonSearch[1])
                };
            }

            // この戦闘の開始前HPを取得
            if(nowhps != null) {
                for (int i = 1; i < nowhps.length; i++) {
                    int hp = nowhps[i];
                    int maxHp = maxhps[i];
                    if (i <= 6) {
                        if (i <= numFships) {
                            this.maxFriendHp[i - 1] = maxHp;
                            this.friendGaugeMax += this.startFriendHp[i - 1] = hp;
                        }
                    } else {
                        if ((i - 6) <= numEships) {
                            this.maxEnemyHp[i - 1 - 6] = maxHp;
                            this.enemyGaugeMax += this.startEnemyHp[i - 1 - 6] = hp;
                        }
                    }
                }
            }
            else{
                for(int i=0;i<fNowHps.length;i++){
                    this.maxFriendHp[i] = fMaxHps[i];
                    this.friendGaugeMax += this.startFriendHp[i] = fNowHps[i];
                }
                for(int i=0;i<eNowHps.length;i++){
                    this.maxEnemyHp[i] = eMaxHps[i];
                    this.enemyGaugeMax += this.startEnemyHp[i] = eNowHps[i];
                }
            }


            if (isFriendCombined || isEnemyCombined) {
                if(nowhps != null) {
                    for (int i = 1; i < nowhpsCombined.length; i++) {
                        int hp = nowhpsCombined[i];
                        int maxHp = maxhpsCombined[i];
                        if (i <= 6) {
                            if (i <= numFshipsCombined) {
                                this.maxFriendHpCombined[i - 1] = maxHp;
                                this.friendGaugeMax += this.startFriendHpCombined[i - 1] = hp;
                            }
                        } else {
                            if ((i - 6) <= numEshipsCombined) {
                                this.maxEnemyHpCombined[i - 1 - 6] = maxHp;
                                this.enemyGaugeMax += this.startEnemyHpCombined[i - 1 - 6] = hp;
                            }
                        }
                    }
                }
                else{
                    if(fNowHpsCombined != null) {
                        for (int i = 0; i < fNowHpsCombined.length; i++) {
                            this.maxFriendHpCombined[i] = fMaxHpsCombined[i];
                            this.friendGaugeMax += this.startFriendHpCombined[i] = fNowHpsCombined[i];
                        }
                    }
                    if(eNowHpsCombined != null) {
                        for (int i = 0; i < eNowHpsCombined.length; i++) {
                            this.maxEnemyHpCombined[i] = eMaxHpsCombined[i];
                            this.enemyGaugeMax += this.startEnemyHpCombined[i] = eNowHpsCombined[i];
                        }
                    }
                }
            }
            if (isFriendCombined) {
                // 退避（連合艦隊は今の所最大が6+6=12隻なのでそれで固定）
                this.escaped = new boolean[12];
                if (tree.containsKey("api_escape_idx")) {
                    for (int jsonShip : GsonUtil.toIntArray(tree.get("api_escape_idx"))) {
                        this.escaped[jsonShip - 1] = true;
                    }
                }
                if (tree.containsKey("api_escape_idx_combined")) {
                    for (int jsonShip : GsonUtil.toIntArray(tree.get("api_escape_idx_combined"))) {
                        this.escaped[jsonShip - 1 + 6] = true;
                    }
                }
                for (int i = 0; i < 2; ++i) {
                    this.friends.get(i).setEscaped(Arrays.copyOfRange(this.escaped, i * 6, (i + 1) * 6));
                }
            }
            else{
                this.escaped = new boolean[7];
                if (tree.containsKey("api_escape_idx")) {
                    for (int jsonShip : GsonUtil.toIntArray(tree.get("api_escape_idx"))) {
                        this.escaped[jsonShip - 1] = true;
                    }
                }
                this.friends.get(0).setEscaped(this.escaped);
            }
        }

        if (this.phaseList.size() > 0) {
            Phase phase = this.phaseList.get(0);
            this.completeDamageAndAddPhase(new Phase(this, tree, json, kind,
                    phase.getNowFriendHp(), phase.getNowFriendHpCombined(),
                    phase.getNowEnemyHp(), phase.getNowEnemyHpCombined()), kind);
        }
        else {
            this.completeDamageAndAddPhase(new Phase(this, tree, json, kind,
                    this.startFriendHp, this.startFriendHpCombined,
                    this.startEnemyHp, this.startEnemyHpCombined), kind);
        }
        return this.phaseList.get(this.phaseList.size() - 1);
    }


    private void completeDamageAndAddPhase(Phase phase, BattlePhaseKind kind) {
        if (kind.isPractice()) {
            phase.practiceDamage(this);
        }
        else {
            phase.battleDamage(this);
        }
        this.phaseList.add(phase);
    }

    private void readResultJson(JsonObject object) {
        if (object.get("api_quest_name") != null) {
            this.questName = object.getString("api_quest_name");
        }
        else {
            // 演習の場合はない
            this.questName = null;
        }
        this.rank = ResultRank.fromRank(object.getString("api_win_rank"));
        // 完全勝利Sは分からないので戦闘結果を見る
        Phase lastPhase = this.getLastPhase();
        if ((lastPhase != null) && (lastPhase.getEstimatedRank() == ResultRank.PERFECT)) {
            this.rank = ResultRank.PERFECT;
        }
        this.enemyName = object.getJsonObject("api_enemy_info").getString("api_deck_name");
        this.dropShip = object.containsKey("api_get_ship");
        this.dropItem = object.containsKey("api_get_useitem");
        if (this.dropShip) {
            JsonObject getShip = object.getJsonObject("api_get_ship");
            this.dropShipId = getShip.getInt("api_ship_id");
            this.dropType = getShip.getString("api_ship_type");
            this.dropName = getShip.getString("api_ship_name");
        }
        else {
            this.dropType = "";
            this.dropName = "";
        }
        if (this.dropItem) {
            String name = UseItem.get(object.getJsonObject("api_get_useitem").getInt("api_useitem_id"));
            this.dropItemName = StringUtils.defaultString(name);
        }
        else {
            this.dropItemName = "";
        }
        this.mvp = object.getInt("api_mvp");
        if (JsonUtils.hasKey(object, "api_mvp_combined")) {
            this.mvpCombined = object.getInt("api_mvp_combined");
        }
        this.hqLv = object.getInt("api_member_lv");
        if (JsonUtils.hasKey(object, "api_escape")) {
            JsonObject jsonEscape = object.getJsonObject("api_escape");
            if(jsonEscape.containsKey("api_tow_idx")) {
                this.escapeInfo = new int[]{
                        jsonEscape.getJsonArray("api_escape_idx").getInt(0) - 1,
                        jsonEscape.getJsonArray("api_tow_idx").getInt(0) - 1
                };
            }else{
                this.escapeInfo = new int[]{
                        jsonEscape.getJsonArray("api_escape_idx").getInt(0) - 1
                };
            }
        }
        if (JsonUtils.hasKey(object, "api_lost_flag")) {
            this.lostflag = new boolean[6];
            JsonArray jsonLostflag = object.getJsonArray("api_lost_flag");
            for (int i = 1; i < jsonLostflag.size(); i++) {
                this.lostflag[i - 1] = (jsonLostflag.getInt(i) != 0);
            }
        }
    }


    private void readResultJson2(LinkedTreeMap tree) {
        Object questName = tree.get("api_quest_name");
        if (questName instanceof String) {
            this.questName = (String)questName;
        }
        else {
            // 演習の場合はない
            this.questName = null;
        }
        this.rank = ResultRank.fromRank(tree.get("api_win_rank").toString());
        // 完全勝利Sは分からないので戦闘結果を見る
        Phase lastPhase = this.getLastPhase();
        if ((lastPhase != null) && (lastPhase.getEstimatedRank() == ResultRank.PERFECT)) {
            this.rank = ResultRank.PERFECT;
        }
        this.enemyName = ((LinkedTreeMap)tree.get("api_enemy_info")).get("api_deck_name").toString();
        this.dropShip = tree.containsKey("api_get_ship");
        this.dropItem = tree.containsKey("api_get_useitem");
        if (this.dropShip) {
            LinkedTreeMap getShip = (LinkedTreeMap)tree.get("api_get_ship");
            this.dropShipId = GsonUtil.toInt(getShip.get("api_ship_id"));
            this.dropType = getShip.get("api_ship_type").toString();
            this.dropName = getShip.get("api_ship_name").toString();
        }
        else {
            this.dropType = "";
            this.dropName = "";
        }
        if (this.dropItem) {
            String name = UseItem.get(GsonUtil.toInt(((LinkedTreeMap)tree.get("api_get_useitem")).get("api_useitem_id")));
            this.dropItemName = StringUtils.defaultString(name);
        } else {
            this.dropItemName = "";
        }
        this.mvp = GsonUtil.toInt(tree.get("api_mvp"));
        if (tree.get("api_mvp_combined")!=null) {
            this.mvpCombined = GsonUtil.toInt(tree.get("api_mvp_combined"));
        }
        this.hqLv = GsonUtil.toInt(tree.get("api_member_lv"));
        if (tree.get("api_escape")!=null) {
            LinkedTreeMap escape = (LinkedTreeMap)tree.get("api_escape");
            if(escape.containsKey("api_tow_idx")) {
                this.escapeInfo = new int[]{
                        GsonUtil.toIntArray(escape.get("api_escape_idx"))[0] - 1,
                        GsonUtil.toIntArray(escape.get("api_tow_idx"))[0] - 1
                };
            }
            else {
                this.escapeInfo = new int[]{
                        GsonUtil.toIntArray(escape.get("api_escape_idx"))[0] - 1
                };
            }
        }
        if (tree.get("api_lost_flag")!=null) {
            this.lostflag = new boolean[6];
            int[] lostflagArray = GsonUtil.toIntArray(tree.get("api_lost_flag"));
            for (int i = 1; i < lostflagArray.length; i++) {
                this.lostflag[i - 1] = (lostflagArray[i] != 0);
            }
        }
    }

    /**
     * 戦闘結果を読み込む
     * @param object 受け取ったJSON
     * @param mapInfo マス情報
     */
    public void setResult(JsonObject object, MapCellDto mapInfo) {
        this.resultJson = object.toString();
        this.mapCellDto = mapInfo;
        this.readResultJson(object);
    }

    public static String toFormation(int f) {
        String formation;
        switch (f) {
        case 1:
            formation = "単縦陣";
            break;
        case 2:
            formation = "複縦陣";
            break;
        case 3:
            formation = "輪形陣";
            break;
        case 4:
            formation = "梯形陣";
            break;
        case 5:
            formation = "単横陣";
            break;
        case 6:
            formation = "警戒陣";
            break;
        case 11:
            formation = "第一警戒航行序列";
            break;
        case 12:
            formation = "第二警戒航行序列";
            break;
        case 13:
            formation = "第三警戒航行序列";
            break;
        case 14:
            formation = "第四警戒航行序列";
            break;
        default:
            formation = "不明(" + f + ")";
            break;
        }
        return formation;
    }

    public static int fromFormation(String f) {
        if (f.startsWith("単縦"))
            return 1;
        else if (f.startsWith("複縦"))
            return 2;
        else if (f.startsWith("輪形"))
            return 3;
        else if (f.startsWith("梯形"))
            return 4;
        else if (f.startsWith("単横"))
            return 5;
        else if (f.startsWith("第一警戒"))
            return 11;
        else if (f.startsWith("第二警戒"))
            return 12;
        else if (f.startsWith("第三警戒"))
            return 13;
        else if (f.startsWith("第四警戒"))
            return 14;
        else
            return 1;
    }

    public static String toMatch(int id) {
        switch (id) {
        case 1:
            return "同航戦";
        case 2:
            return "反航戦";
        case 3:
            return "Ｔ字有利";
        case 4:
            return "Ｔ字不利";
        default:
            return "不明(" + id + ")";
        }
    }

    public static String toSupport(int id) {
        switch (id) {
        case 1:
            return "航空支援";
        case 2:
            return "支援射撃";
        case 3:
            return "支援長距離雷撃";
        default:
            return "不明(" + id + ")";
        }
    }

    public static String toSearch(int id) {
        switch (id) {
        case 1:
            return "発見!";
        case 2:
            return "発見!索敵機未帰還機あり";
        case 3:
            return "発見できず…索敵機未帰還機あり";
        case 4:
            return "発見できず…";
        case 5:
            return "発見!(索敵機なし)";
        case 6:
            return "なし";
        default:
            return "不明(" + id + ")";
        }
    }

    /**
     * 保存用エネミーデータ作成
     * @param enemyId
     * @param enemyName
     * @return
     */
    public EnemyData getEnemyData(int enemyId, String enemyName) {
        int[] enemyShips = new int[] { -1, -1, -1, -1, -1, -1 };
        for (int i = 0; i < this.enemy.size(); ++i) {
            enemyShips[i] = this.enemy.get(i).getShipId();
        }
        return new EnemyData(enemyId, enemyName, enemyShips, this.formation[1]);
    }

    /**
     * 味方は連合艦隊か？
     * @return
     */
    public boolean isCombined() {
        return (this.startFriendHpCombined != null);
    }

    /**
     * 敵は連合艦隊か？
     * @return
     */
    public boolean isEnemyCombined() {
        return (this.startEnemyHpCombined != null);
    }

    /**
     * 最後に行ったフェーズを取得
     * @return
     */
    public Phase getLastPhase() {
        if (this.phaseList.size() == 0)
            return null;
        return this.phaseList.get(this.phaseList.size() - 1);
    }

    /**
     * 最初のフェーズを取得
     * @return
     */
    public Phase getPhase1() {
        if (this.phaseList.size() < 1)
            return null;
        return this.phaseList.get(0);
    }

    /**
     * ２番目のフェーズ（ない時はnull）
     * @return
     */
    public Phase getPhase2() {
        if (this.phaseList.size() < 2)
            return null;
        return this.phaseList.get(1);
    }

    /**
     * 戦闘結果も含んでいるか
     * これがfalseに場合は正常に記録されない
     * @return
     */
    public boolean isCompleteResult() {
        if (this.questName != null) {
            // 出撃の場合
            if (this.mapCellDto == null) {
                return false;
            }
        }
        else {
            // 演習の場合
        }
        return (this.friends != null) && (this.getDock() != null) &&
                (this.rank != null) && (this.phaseList != null) &&
                (this.phaseList.size() > 0);
    }

    /**
     * 演習か？
     * @return
     */
    public boolean isPractice() {
        return (this.questName == null);
    }

    /**
     * 交戦後の味方艦HP（連合艦隊の時は第一艦隊）
     * @return
     */
    public int[] getNowFriendHp() {
        return this.getLastPhase().getNowFriendHp();
    }

    /**
     * 交戦後の味方艦HP（連合艦隊でないときはnull）
     * @return
     */
    public int[] getNowFriendHpCombined() {
        return this.getLastPhase().getNowFriendHpCombined();
    }

    /**
     * 交戦後の敵艦HP
     * @return
     */
    public int[] getNowEnemyHp() {
        return this.getLastPhase().getNowEnemyHp();
    }

    /**
     * 交戦後の敵艦HP 連合艦隊第二艦隊
     * @return
     */
    public int[] getNowEnemyHpCombined() {
        return this.getLastPhase().getNowEnemyHpCombined();
    }

    /**
     * 味方艦隊（連合艦隊の時は第一艦隊）
     * @return
     */
    public DockDto getDock() {
        return this.friends.get(0);
    }

    /**
     * 連合艦隊第二艦隊（連合艦隊でないときはnull）
     * @return
     */
    public DockDto getDockCombined() {
        if (this.friends.size() < 2)
            return null;
        return this.friends.get(1);
    }

    /**
     * 戦闘のあった日時
     * @return battleDate
     */
    public Date getBattleDate() {
        return this.battleDate;
    }

    /**
     * 味方艦隊
     * @return friends
     */
    public List<DockDto> getFriends() {
        return this.friends;
    }

    /**
     * 敵艦
     * @return enemy
     */
    public List<EnemyShipDto> getEnemy() {
        return this.enemy;
    }

    /**
     * 敵艦 連合艦隊第二艦隊
     * @return enemy
     */
    public List<EnemyShipDto> getEnemyCombined() {
        return this.enemyCombined;
    }

    /**
     * 味方艦のMaxHP
     * 連合艦隊の時は第一艦隊のみ
     * @return maxFriendHp
     */
    public int[] getMaxFriendHp() {
        return this.maxFriendHp;
    }

    /**
     * 味方連合艦隊第二艦隊のMaxHP
     * @return maxFriendHpCombined
     */
    public int[] getMaxFriendHpCombined() {
        return this.maxFriendHpCombined;
    }

    /**
     * 敵艦のMaxHP
     * @return maxEnemyHp
     */
    public int[] getMaxEnemyHp() {
        return this.maxEnemyHp;
    }

    /**
     * 敵連合艦隊第二艦隊のMaxHP
     * @return maxEnemyHpCombined
     */
    public int[] getMaxEnemyHpCombined() {
        return this.maxEnemyHpCombined;
    }

    /**
     * 戦闘開始時の味方艦のHP
     * 連合艦隊の時は第一艦隊のみ
     * @return startFriendHp
     */
    public int[] getStartFriendHp() {
        return this.startFriendHp;
    }

    /**
     * 味方連合艦隊第二艦隊の戦闘開始時HP
     * @return startFriendHpCombined
     */
    public int[] getStartFriendHpCombined() {
        return this.startFriendHpCombined;
    }

    /**
     * 敵艦の戦闘開始時HP
     * @return startEnemyHp
     */
    public int[] getStartEnemyHp() {
        return this.startEnemyHp;
    }

    /**
     * 敵艦の戦闘開始時HP 連合艦隊第二艦隊
     * @return startEnemyHpCombined
     */
    public int[] getStartEnemyHpCombined() {
        return this.startEnemyHpCombined;
    }

    /**
     * 味方戦果ゲージの最大（味方艦MaxHPの合計）
     * @return friendGaugeMax
     */
    public int getFriendGaugeMax() {
        return this.friendGaugeMax;
    }

    /**
     * 敵戦果ゲージの最大（敵艦MaxHPの合計）
     * @return enemyGaugeMax
     */
    public int getEnemyGaugeMax() {
        return this.enemyGaugeMax;
    }

    /**
     * 陣形 [味方, 敵]
     * @return formation
     */
    public String[] getFormation() {
        return this.formation;
    }

    /**
     * 同航戦、反航戦など
     * @return formationMatch
     */
    public String getFormationMatch() {
        return this.formationMatch;
    }

    /**
     * 索敵状況 [味方, 敵]
     * @return sakuteki
     */
    public String[] getSakuteki() {
        return this.sakuteki;
    }

    /**
     * 出撃海域情報
     * @return questName
     */
    public String getQuestName() {
        return this.questName;
    }

    /**
     * 戦闘結果のランク
     * @return rank
     */
    public ResultRank getRank() {
        return this.rank;
    }

    /**
     * 戦闘のあったマスの情報
     * @return mapCelldto
     */
    public MapCellDto getMapCellDto() {
        return this.mapCellDto;
    }

    /**
     * 敵艦隊の名前
     * @return enemyName
     */
    public String getEnemyName() {
        return this.enemyName;
    }

    /**
     * ドロップ艦があったか？
     * @return dropShip
     */
    public boolean isDropShip() {
        return this.dropShip;
    }

    /**
     * ドロップアイテムがあったか？
     * @return dropItem
     */
    public boolean isDropItem() {
        return this.dropItem;
    }

    /**
     * ドロップ艦の艦種（アイテムの場合は「アイテム」）
     * @return dropType
     */
    public String getDropType() {
        return this.dropType;
    }

    /**
     * ドロップ艦の名前
     * @return dropName
     */
    public String getDropName() {
        return this.dropName;
    }

    /**
     * ドロップアイテムの名前
     * @return dropItemName
     */
    public String getDropItemName() {
        return this.dropItemName;
    }

    /**
     * 戦闘フェーズ（昼戦・夜戦）リスト
     * @return phaseList
     */
    public List<Phase> getPhaseList() {
        return this.phaseList;
    }

    /**
     * MVP艦が何番目の艦か (1～)
     * MVPがいない時は-1
     * @return mvp
     */
    public int getMvp() {
        return this.mvp;
    }

    /**
     * 連合艦隊第二艦隊のMVP艦が何番目の艦か
     * 連合艦隊でない時またはMVPがいない時は-1
     * @return mvpCombined
     */
    public int getMvpCombined() {
        return this.mvpCombined;
    }

    /**
     * 司令部Lv
     * @return hqLv
     */
    public int getHqLv() {
        return this.hqLv;
    }

    /***
     * BattleExDtoのバージョン
     * exVersion == 0 : Tag 34以降がない
     * exVersion == 1 : Tag 36まである
     * exVersion == 2 : Jsonがある
     * @return exVersion
     */
    public int getExVersion() {
        return this.exVersion;
    }

    void setExVersion(int exVersion) {
        this.exVersion = exVersion;
    }

    /**
     * 母港の艦娘空き枠
     * @return shipSpace
     */
    public int getShipSpace() {
        return this.shipSpace;
    }

    /**
     * 母港の装備アイテム空き枠
     * @return itemSpace
     */
    public int getItemSpace() {
        return this.itemSpace;
    }

    /**
     * 連合艦隊における退避意見 [退避する艦(0-11), 護衛艦(0-11)]
     * @return escapeInfo
     */
    public int[] getEscapeInfo() {
        return this.escapeInfo;
    }

    /**
     * 護衛退避で戦線離脱したか [第1艦隊1番艦～第2艦隊6番艦]
     * 艦隊の艦数に関係なく常に長さは12
     * @return escaped
     */
    public boolean[] getEscaped() {
        return this.escaped;
    }

    /**
     * 戦闘結果のレスポンスJSON
     * @return resultJson
     */
    public JsonObject getResultJson() {
        if (this.resultJson == null) {
            return null;
        }
        return JsonUtils.fromString(this.resultJson);
    }

    /**
     * @return lostflag
     */
    public boolean[] getLostflag() {
        return this.lostflag;
    }

    /**
     * @return shipId
     */
    public int getDropShipId() {
        return this.dropShipId;
    }

    //標準装備のスクリプト群については全部Java側で処理することにした
    private static Gson _gson = new Gson();
    private static Gson getGson(){
        return _gson;
    }

    /**
     * 連合艦隊フラグ 連合の種類が特定できなかった場合は-1
     * 過去の記録には保存されていないはずなので編成から判断するが決定不能なケースもよくある
     * @return combinedFlag
     */
    private int calcCombinedFlag(){
        if(this.isCombined() == false){
            return 0;
        }

        if(this.phaseList.isEmpty()){
            return -1;
        }
        Phase phase = this.phaseList.get(0);
        if(phase.isNight()){
            return -1;
        }
        else if(phase.getApi().equals(DataType.COMBINED_BATTLE_WATER.getApiName())||phase.getApi().equals(DataType.COMBINED_EACH_BATTLE_WATER.getApiName())){
            return 2;
        }
        else if(phase.getApi().equals(DataType.COMBINED_BATTLE.getApiName())||phase.getApi().equals(DataType.COMBINED_EACH_BATTLE.getApiName())){
            //輸送と機動の区別は編成条件によるゴリ押し消去法しかない?
            //空き枠は轟沈考慮
            if(this.getDock() == null || this.getDockCombined() == null){
                return -1;
            }
            {
                int type1[];
                {
                    List<ShipDto> ships1 = this.getDock().getShips();
                    type1 =  new int[ships1.size()];
                    for(int i=0;i<type1.length;i++){
                        type1[i] = ships1.get(i).getStype();
                    }
                }
                int aki = 6-type1.length;
                {
                    int kuubo = 0;
                    int senkan = 0;
                    for(int type:type1){
                        switch(type){
                            case 7:
                            case 11:
                            case 18:
                                kuubo++;break;
                            case 8:
                            case 9:
                            case 10:
                                senkan++;break;
                        }
                    }
                    if(kuubo + aki < 2 || 4 < kuubo || 2 < senkan){
                        return 3;
                    }
                }
                {
                    int kuchiku = 0;
                    int keijunRenjun = 0;
                    int koujun = 0;
                    int kousen = 0;
                    int suibo = 0;
                    int youriku = 0;
                    int senbo = 0;
                    int hokyu = 0;
                    int kuubo = 0;
                    int sonota = 0;
                    for(int type:type1){
                        switch(type){
                            case 2:
                                kuchiku++;break;
                            case 3:
                            case 21:
                                keijunRenjun++;break;
                            case 6:
                                koujun++;break;
                            case 10:
                                kousen++;break;
                            case 16:
                                suibo++; break;
                            case 17:
                                youriku++;break;
                            case 20:
                                senbo++;break;
                            case 22:
                                hokyu++;break;
                            case 7:
                            case 11:
                            case 18:
                                kuubo++;break;
                            default:
                                sonota++;break;
                        }
                    }
                    if(kuchiku + aki < 4 || 2 < keijunRenjun || 2 < koujun || 2 < kousen || 2 < suibo || 1 < youriku || 1 < senbo || 1 < hokyu || 1<kuubo || 0 < sonota ){
                        return 1;
                    }
                }
            }
            {
                int type2[];
                {
                    List<ShipDto> ships2 = this.getDockCombined().getShips();
                    type2 =  new int[ships2.size()];
                    for(int i=0;i<type2.length;i++){
                        type2[i] = ships2.get(i).getStype();
                    }
                }
                int aki = 6-type2.length;
                {
                    int keijun = 0;
                    int kuchiku = 0;
                    int jujunKoujun = 0;
                    int keikuubo = 0;
                    int suibo = 0;
                    int kousokuSenkan = 0;
                    int teisokuSenkanKousenSeikikuubo = 0;
                    for(int type:type2){
                        switch(type){
                            case 3:
                                keijun++;break;
                            case 2:
                                kuchiku++;break;
                            case 5:
                            case 6:
                                jujunKoujun++;break;
                            case 7:
                                keikuubo++;break;
                            case 16:
                                suibo++;break;
                            case 8:
                                kousokuSenkan++;break;
                            case 9:
                            case 10:
                            case 11:
                                teisokuSenkanKousenSeikikuubo++;break;
                        }
                    }
                    if( keijun + aki < 1 || kuchiku + aki < 2 || 1 < keijun || 2 < jujunKoujun || 1 < keikuubo || 1 < suibo || 2 < kousokuSenkan || 0 < teisokuSenkanKousenSeikikuubo ){
                        return 3;
                    }
                }
                {
                    int keijunRenjun = 0;
                    int kuchiku = 0;
                    int jujunKoujun = 0;
                    int sonota = 0;
                    for(int type:type2){
                        switch(type){
                            case 3:
                            case 21:
                                keijunRenjun++;break;
                            case 2:
                                kuchiku++;break;
                            case 5:
                            case 6:
                                jujunKoujun++;break;
                            default:
                                sonota++;break;
                        }
                    }
                    if( keijunRenjun + aki < 1 || kuchiku + aki < 3 || 2 < keijunRenjun || 2 < jujunKoujun || 0 < sonota ){
                        return 1;
                    }
                }
            }
            return -1;
        }
        else{
            return -1;
        }
    }
    /**
     * 連合艦隊の種類を取得します
     * @return 連合艦隊の種類(0:未結成、1:機動部隊、2:水上部隊、3:輸送部隊、-x:強制解隊)
     */
    public int getCombinedKind() {
        if(this.isCombined()){
            if(this.combinedKind > 0){
                return this.combinedKind;
            }
            else{
                return this.calcCombinedFlag();
            }
        }else{
            return this.combinedKind;
        }
    }

    public void setCombinedKind(int combinedKind) {
        this.combinedKind = combinedKind;
    }

    /**
     * 第一艦隊の最大隻数＝第2艦隊のベースインデックス
     * @return numFirstMax
     */
    public int getSecondBase() {
        if (this.secondBase == 0) {
            // 旧データ対応
            return 6;
        }
        return this.secondBase;
    }

    public boolean isSplistHp(){
        if(this.phaseList.size() == 0){
            return false;
        }
        LinkedTreeMap tree = this.phaseList.get(0).getTree();
        if(tree == null){
            return false;
        }
        return tree.containsKey("api_f_nowhps");
    }
}
