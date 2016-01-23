
declare module Packages.logbook.dto {

    /**
     * 装備を表します
     */
    class ItemInfoDto {

        isPlane(): boolean;

        /**
         * @return 表示分類名
         */
        getTypeName(): string;

        /**
         * typeを取得します。
         * @return type
         */
        getType(): JavaArray<number>;

        /**
         * type[0]: 大分類（砲、魚雷、艦載機、...）
         * @return type0
         */
        getType0(): number;

        /**
         * type[1]: 図鑑の背景にある英語表記分類
         * @return type1
         */
        getType1(): number;

        /**
         * type[2]: 装備可能艦種別分類
         * @return type2
         */
        getType2(): number;

        /**
         * type[3]: アイコンの分類
         * @return type3
         */
        getType3(): number;

        /**
         * slotitem_id
         * @return id
         */
        getId(): number;

        /**
         * 名前
         * @return name
         */
        getName(): string;

        /**
         * 装備のパラメータ
         * @return param
         */
        getParam(): ShipParameters;
    }
}