
declare module Packages.logbook.dto {

    /**
     * @author Nekopanda
     * 味方艦・敵艦のベースクラス
     */
    class ShipBaseDto {

        isFriend(): boolean;

        /**
         * 艦のマスター情報を取得
         * @return shipInfo
         */
        getShipInfo(): ShipInfoDto;

        /**
         * 艦娘を識別するID
         * @return 艦娘を識別するID
         */
        getShipId(): number;

        /**
         * 名前
         * @return 名前
         */
        getName(): number;

        /**
         * 航海日誌における表示名
         * @return 表示名
         */
        getFriendlyName(): number;

        /**
         * レベル
         * @return
         */
        getLv(): number;

        /**
         * 艦種
         * @return 艦種
         */
        getType(): string;

        /**
         * 艦種ID
         * @return 艦種
         */
        getStype(): number;

        /**
         * 弾Max
         * @return 弾Max
         */
        getBullMax(): number;

        /**
         * 燃料Max
         * @return 燃料Max
         */
        getFuelMax(): number;

        /**
         * 現在の艦載機搭載数
         * @return 現在の艦載機搭載数
         */
        getOnSlot(): JavaArray<number>;

        /**
         * 艦載機最大搭載数
         * @return 艦載機最大搭載数
         */
        getMaxeq(): JavaArray<number>;

        /**
         * 装備（名前）
         * @return 装備
         */
        getSlot(): JavaArray<number>;

        /**
         * 装備ID
         * 艦娘の場合は 装備個別ID
         * 敵艦の場合は 装備ID (slotitem_id)
         * @return 装備ID
         */
        getItemId(): JavaArray<number>;

        /**
         * 装備(マスターデータ)
         * @return 装備
         */
        getItem(): Packages.java.util.List<ItemInfoDto>;

        /**
         * 装備(個別)
         * @return slotItem2
         */
        getItem2(): Packages.java.util.List<ItemDto>;

        /**
         * 制空値
         * @return 制空値
         */
        setSeiku(): number;

        /**
         * アイテムの索敵合計を計算します
         * @return アイテムの索敵合計
         */
        getSlotSakuteki(): number;

        /**
         * /ドラム缶の合計を計算します
         * @return ドラム缶の合計値
         */
        getDram(): number;

        /**
         * /大発の合計を計算します
         * @return 大発の合計値
         */
        getDaihatsu(): number;

        /**
         * 飛行機を装備できるか？
         * @return 飛行機を装備できるか？
         */
        canEquipPlane(): boolean;

        /**
         * 名前:装備1,装備2,...
         * @return
         */
        getDetailedString(): string;

        /**
         * 火力
         * @return 火力
         */
        getKaryoku(): number;

        /**
         * 火力(最大)(艦娘のみ)
         * @return 火力(最大)(艦娘のみ)
         */
        getKaryokuMax(): number;

        /**
         * 雷装
         * @return 雷装
         */
        getRaisou(): number;

        /**
         * 雷装(最大)(艦娘のみ)
         * @return 雷装(最大)(艦娘のみ)
         */
        getRaisouMax(): number;

        /**
         * 対空
         * @return 対空
         */
        getTaiku(): number;

        /**
         * 対空(最大)(艦娘のみ)
         * @return 対空(最大)(艦娘のみ)
         */
        getTaikuMax(): number;

        /**
         * 装甲
         * @return 装甲
         */
        getSoukou(): number;

        /**
         * 装甲(最大)(艦娘のみ)
         * @return 装甲(最大)(艦娘のみ)
         */
        getSoukouMax(): number;

        /**
         * 回避
         * @return 回避
         */
        getKaihi(): number;

        /**
         * 回避(最大)(艦娘のみ)
         * @return 回避(最大)(艦娘のみ)
         */
        getKaihiMax(): number;

        /**
         * 対潜
         * @return 対潜
         */
        getTaisen(): number;

        /**
         * 対潜(最大)(艦娘のみ)
         * @return 対潜(最大)(艦娘のみ)
         */
        getTaisenMax(): number;

        /**
         * 索敵
         * @return 索敵
         */
        getSakuteki(): number;

        /**
         * 索敵(最大)(艦娘のみ)
         * @return 索敵(最大)(艦娘のみ)
         */
        getSakutekiMax(): number;

        /**
         * 運
         * @return 運
         */
        getLucky(): number;

        /**
         * 運(最大)(艦娘のみ)
         * @return 運(最大)(艦娘のみ)
         */
        getLuckyMax(): number;

        /**
         * 装備込のパラメータ
         * @return 装備込のパラメータ
         */
        getParam(): ShipParameters;

        /**
         * 装備による上昇分
         * @return 装備による上昇分
         */
        getSlotParam(): ShipParameters;

        /**
         * この艦の最大パラメータ（装備なしで）
         * @return この艦の最大パラメータ（装備なしで）
         */
        getMax(): ShipParameters;

        /**
         * @return
         */
        getFullName(): string;
    }
}