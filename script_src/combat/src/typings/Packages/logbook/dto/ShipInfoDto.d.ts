
declare module Packages.logbook.dto {

    /**
     * 艦娘の名前と種別を表します
     *
     */
    class ShipInfoDto {

        getFullName(): string;

        getDefaultSlot(): JavaArray<number>;

        /**
         * 名前を取得します。
         * @return 名前
         */
        getName(): string;

        /**
         * 艦IDを取得します。
         */
        getShipId(): number;

        /**
         * 艦種を取得します。
         */
        getStype(): number;

        /**
         * 艦種を取得します。
         * @return 艦種
         */
        getType(): number;

        /**
         * @return 改造レベル(改造ができない場合、0)
         */
        getAfterLv(): number;

        /**
         * @return 改造後の艦ID(改造ができない場合、0)
         */
        getAftershipId(): number;

        /**
         * flagshipもしくはelite (敵艦のみ)を取得します。
         * @return flagshipもしくはelite (敵艦のみ)
         */
        getFlagship(): number;

        /**
         * 弾を取得します。
         * @return 弾
         */
        getMaxBull(): number;

        /**
         * 燃料を取得します。
         * @return 燃料
         */
        getMaxFuel(): number;

        /**
         * powupを取得します。
         * @return powup
         */
        getPowup(): JavaArray<number>;

        /**
         * maxeqを取得します。
         * @return maxeq
         */
        getMaxeq(): JavaArray<number>;

        /**
         * @return param
         */
        getParam(): ShipParameters;

        /**
         * @return max
         */
        getMax(): ShipParameters;

        /**
         * @return json
         */
        getJson(): Packages.javax.json.JsonObject;

        /**
         * @return sortNo
         */
        getSortNo(): number;

        /**
         * @return slotNum
         */
        getSlotNum(): number;
    }
}