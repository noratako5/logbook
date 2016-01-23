
declare module Packages.logbook.dto {

    /**
     * 艦娘を表します
     *
     */
    class ShipDto extends ShipBaseDto {

        /**
         * 艦娘個人を識別するID
         * @return 艦娘個人を識別するID
         */
        getId(): number;

        /**
         * 艦娘キャラを識別するID
         * @return 艦娘キャラを識別するID
         */
        getCharId(): number;

        /**
         * sortno
         * @return sortno
         */
        getSortno(): number;

        /**
         * 鍵付き
         * @return 鍵付き
         */
        getLocked(): boolean;

        /**
         * @return 艦隊ID
         */
        getFleetid(): string;

        /**
         * 艦隊に所属しているか？
         * @return
         */
        isFleetMember(): boolean;

        /**
         * 艦隊での位置
         * @return fleetpos
         */
        isFleetpos(): number;

        /**
         * Lv
         * @return Lv
         */
        getLv(): number;

        /**
         * 疲労
         * @return 疲労
         */
        getCond(): number;

        /**
         * 現在の疲労推定値（下限値）
         * @return 現在の疲労推定値（下限値）
         */
        getEstimatedCond(timer: Packages.logbook.internal.CondTiming): number;

        /**
         * 入渠時間
         * @return 入渠時間
         */
        getDocktime(): number;

        /**
         * 泊地修理による修理時間
         * @return
         */
        getAkashiTime(): number;

        /**
         * 修復資材 燃料
         * @return 修復資材 燃料
         */
        getDockfuel(): number;

        /**
         * 修復資材 鋼材
         * @return 修復資材 鋼材
         */
        getDockmetal(): number;

        /**
         * 残弾
         * @return 残弾
         */
        getBull(): number;

        /**
         * 燃料
         * @return 燃料
         */
        getFuel(): number;

        /**
         * 経験値
         * @return 経験値
         */
        getExp(): number;

        /**
         * 経験値ゲージの割合
         * @return 経験値ゲージの割合
         */
        getExpratio(): number;

        /**
         * 現在のHP
         * @return HP
         */
        getNowhp(): number;

        /**
         * 最大HP
         * @return MaxHP
         */
        getMaxhp(): number;

        /**
         * 装備可能スロット数
         * @return
         */
        getSlotNum(): number;

        /**
         * 現在の艦載機搭載数
         * @return 現在の艦載機搭載数
         */
        getOnSlot(): JavaArray<number>;

        /**
         * 次のレベルまでの経験値
         * @return 次のレベルまでの経験値
         */
        getNext(): number;

        /**
         * 疲労が抜けるまでの時間
         * @return 疲労が抜けるまでの時間
         */
        getCondClearTime(timer: Packages.logbook.internal.CondTiming, okCond?: number): Packages.java.util.Date;

        /**
         * 艦娘が轟沈しているかを調べます
         * @return 轟沈したの場合
         */
        isSunk(): boolean;

        /**
         * 艦娘が大破しているかを調べます
         * @return 大破以上の場合
         */
        isBadlyDamage(): boolean;

        /**
         * 艦娘が中破しているかを調べます
         * @return 中破以上の場合
         */
        isHalfDamage(): boolean;

        /**
         * 艦娘が小破しているかを調べます
         * @return 小破以上の場合
         */
        isSlightDamage(): boolean;

        /**
         * 装備で加算された命中
         * @return 装備の命中
         */
        getAccuracy(): number;

        /**
         * 砲撃戦火力
         * @return 砲撃戦火力
         */
        getHougekiPower(): number;

        /**
         * 雷撃戦火力
         * @return 雷撃戦火力
         */
        getRaigekiPower(): number;

        /**
         * 対潜火力
         * @return 対潜火力
         */
        getTaisenPower(): number;

        /**
         * 夜戦火力
         * @return 夜戦火力
         */
        getYasenPower(): number;

        /**
         * データの更新に使ったJSON
         * @return json
         */
        getJson(): Packages.javax.json.JsonObject;

        /**
         * 補助装備ID
         * @return
         */
        getSlotEx(): number;

        /**
         * 補助装備
         * @return
         */
        getSlotExItem(): ItemDto;
    }
}