
declare module kcsapi {

    interface RaigekiBattleApi {

        /** 雷撃ターゲット */
        api_frai: number[];

        /**  */
        api_erai: number[];

        /** 被ダメージ */
        api_fdam: number[];

        /**  */
        api_edam: number[];

        /** 与ダメージ */
        api_fydam: number[];

        /**  */
        api_eydam: number[];

        /** クリティカルヒット？ */
        api_fcl: number[];

        /**  */
        api_ecl: number[];
    }
}