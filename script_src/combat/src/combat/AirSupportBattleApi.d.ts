
declare module combat {

    interface AirSupportBattleApi {

        /** 艦隊ID */
        api_dock_id: number | string;

        /** 艦隊ID */
        api_deck_id: number | string;

        /** 艦船固有ID */
        api_ship_id: number[];

        /** 中破グラフィックフラグ 0= 通常, 1 = 中破 なお適用はバナーのみで、旗艦グラフィックは変わらない */
        api_undressing_flag: number[];

        /** >航空戦 */
        api_stage_flag: number[];

        /** 艦載機を飛ばせる艦(敵のみ, 7 - 12) */
        api_plane_from: number[][];

        /** 航空戦stage1に同じ */
        api_stage1: {

            /**  */
            api_f_count: number;

            /**  */
            api_f_lostcount: number;

            /**  */
            api_e_count: number;

            /** 注：wikiの記述とは異なり、敵艦載機は撃墜可能。効果的かはさておき */
            api_e_lostcount: number;
        };

        /** 航空戦stage2に同じ */
        api_stage2: {

            /**  */
            api_f_count: number;

            /**  */
            api_f_lostcount: number;
        };

        /** 航空戦stage3に同じ */
        api_stage3: {

            /**  */
            api_erai_flag: number[];

            /**  */
            api_ebak_flag: number[];

            /** 0=通常(ミス|命中), 1=クリティカル */
            api_ecl_flag: number[];

            /**  */
            api_edam: number[];
        };
    }
}