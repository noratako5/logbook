
declare module Packages.logbook.dto {

    /**
     * 艦や装備のパラメータ
     * @author Nekopanda
     */
    class ShipParameters {

        /**
         * HP
         * @return HP
         */
        getHP(): number;

        /**
         * 火力
         * @return 火力
         */
        getKaryoku(): number;

        /**
         * 雷装
         * @return 雷装
         */
        getRaisou(): number;

        /**
         * 対空
         * @return 対空
         */
        getTaiku(): number;

        /**
         * 装甲
         * @return 装甲
         */
        getSoukou(): number;

        /**
         * 回避
         * @return 回避
         */
        getKaihi(): number;

        /**
         * 対潜
         * @return 対潜
         */
        getTaisen(): number;

        /**
         * 索敵
         * @return 索敵
         */
        getSakuteki(): number;

        /**
         * 運
         * @return 運
         */
        getLucky(): number;

        /**
         * kaih (= houk)
         * @return kaih (= houk)
         */
        getHouk(): number;

        ///////////////////////

        /**
         * @return taik
         */
        getTaik(): number;

        /**
         * @return houg
         */
        getHoug(): number;

        /**
         * @return raig
         */
        getRaig(): number;

        /**
         * @return tyku
         */
        getTyku(): number;

        /**
         * @return souk
         */
        getSouk(): number;

        /**
         * @return kaih
         */
        getKaih(): number;

        /**
         * @return tais
         */
        getTais(): number;

        /**
         * @return saku
         */
        getSaku(): number;

        /**
         * @return luck
         */
        getLuck(): number;

        /**
         * @return soku
         */
        getSoku(): number;

        /**
         * @return leng
         */
        getLeng(): number;

        /**
         * @return houm
         */
        getHoum(): number;

        /**
         * @return baku
         */
        getBaku(): number;
    }
}