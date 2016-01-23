
declare module Packages.logbook.dto {

    /**
     * 攻撃シーケンス
     * @author Nekopanda
     */
    class BattleAtackDto {

        /** 攻撃の種類 */
        kind: AtackKind;

        /** 味方からの攻撃か？ */
        friendAtack: boolean;

        /** 攻撃元(0-11) */
        origin: JavaArray<number>;

        /** 雷撃の攻撃先 */
        ot: JavaArray<number>;

        /** 雷撃の与ダメージ */
        ydam: JavaArray<number>;

        /** 攻撃先(0-11) */
        target: JavaArray<number>;

        /** ダメージ */
        damage: JavaArray<number>;
    }
}