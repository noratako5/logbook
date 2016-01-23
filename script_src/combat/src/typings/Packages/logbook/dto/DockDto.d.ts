
declare module Packages.logbook.dto {

    /**
     * 艦隊のドックを表します
     *
     */
    class DockDto {

        /**
         * ドックIDを取得します。
         * @return ドックID
         */
        getId(): string;

        /**
         * 艦隊名を取得します。
         * @return 艦隊名
         */
        getName(): string;

        /**
         * 艦娘達を取得します。
         * @return 艦娘達
         */
        getShips(): Packages.java.util.List<ShipDto>;

        /**
         * 更新フラグを取得します。
         * @return 更新フラグ
         */
        isUpdate(): boolean;

        /**
         * 大破艦がいるか？を取得します
         * @return 大破艦がいるか？
         */
        isBadlyDamaged(): boolean;

        /**
         * 退避したか？
         * 退避した艦娘がいるときは長さ6の配列
         * 連合艦隊でない場合はnull
         * @return escaped
         */
        getEscaped(): JavaArray<boolean>;

        /**
         * 旗艦が明石か？
         * @return
         */
        isFlagshipAkashi(): boolean;

        /**
         * 泊地修理可能艦数
         * @return
         */
        getAkashiCapacity(): number;
    }
}