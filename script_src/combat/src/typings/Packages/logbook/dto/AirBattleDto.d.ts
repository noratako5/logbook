
declare module Packages.logbook.dto {

    /**
     * 航空戦の情報
     * @author Nekopanda
     */
    class AirBattleDto {

        /** 攻撃シーケンス */
        atacks: Packages.java.util.List<BattleAtackDto>;

        /** 触接 [味方, 敵] */
        touchPlane: JavaArray<number>;

        /** 制空状態 */
        seiku: string;

        /** stage1 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全] */
        stage1: JavaArray<number>;

        /** stage2 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全] */
        stage2: JavaArray<number>;

        getStage1ShortString(): JavaArray<string>;

        getStage2ShortString(): JavaArray<string>;

        getStage1DetailedString(): JavaArray<string>;

        getStage2DetailedString(): JavaArray<string>;

        /**
         * 触接表示を生成 [味方・敵]
         * @param touchPlane
         * @return
         */
        getTouchPlane(): JavaArray<string>;
    }
}