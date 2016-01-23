
declare module Packages.logbook.dto {

    /**
     * 個別装備
     * ロックや改修値などの情報を持つ
     * @author Nekopanda
     */
    class ItemDto {

        /**
         * この装備のマスターデータ
         * @return info
         */
        // Packages.java beans はメソッドで認識するのでここに付ける必要がある
        getInfo(): ItemInfoDto;

        /**
         * 装備ID
         * @return slotitemId
         */
        getSlotitemId(): number;

        /**
         * 装備個別ID
         * @return id
         */
        getId(): number;

        /**
         * ロックされているか？
         * @return locked
         */
        isLocked(): boolean;

        /**
         * 改修値
         * @return level
         */
        getLevel(): number;

        isPlane(): boolean;

        /**
         * 表示分類名
         * @return 表示分類名
         */
        getTypeName(): number;

        /**
         * typeを取得します。
         * @return type
         */
        getType(): JavaArray<number>;

        /**
         * type0を取得します。
         * @return type0
         */
        getType0(): number;

        /**
         * type1を取得します。
         * @return type1
         */
        getType1(): number;

        /**
         * type2を取得します。
         * @return type2
         */
        getType2(): number;

        /**
         * type3を取得します。
         * @return type3
         */
        getType3(): number;

        /**
         * nameを取得します。
         * @return name
         */
        getName(): number;

        /**
         * 航海日誌における表示名を取得
         * @return
         */
        getFriendlyName(): number;

        /**
         * 装備のパラメータ
         * @return
         */
        getParam(): ShipParameters;

        /**
         * @return 熟練度
         */
        getAlv(): number;
    }
}