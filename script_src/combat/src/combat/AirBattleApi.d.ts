
declare module combat {

    interface AirBattleApi {

        /** 艦載機を飛ばせる艦　[0]=味方, [1]=敵　いなければ{-1} */
        api_plane_from: number[][];

        /** 航空戦1　空対空戦闘 */
        api_stage1: {

            /** 味方艦載機数 */
            api_f_count: number;

            /** 〃喪失数 */
            api_f_lostcount: number;

            /** 敵〃 */
            api_e_count: number;

            /**  */
            api_e_lostcount: number;

            /** 制空権表示    1=制空権確保, 4=制空権喪失 */
            api_disp_seiku: number;

            /** 触接装備ID　[0]=味方, [1]=敵? */
            api_touch_plane: number[];
        };

        /** 航空戦2　対空砲火? */
        api_stage2: {

            /** 味方艦載機数(艦攻/艦爆数) */
            api_f_count: number;

            /** 〃喪失数? */
            api_f_lostcount: number;

            /** 敵〃 */
            api_e_count: number;

            /**  */
            api_e_lostcount: number;
        };

        /** 航空攻撃 */
        api_stage3: {

            /** 味方被雷撃フラグ */
            api_frai_flag: number[];

            /** 敵被雷撃フラグ */
            api_erai_flag: number[];

            /** 味方被爆撃フラグ */
            api_fbak_flag: number[];

            /** 敵被爆撃フラグ */
            api_ebak_flag: number[];

            /** 味方?フラグ */
            api_fcl_flag: number[];

            /** 敵?フラグ */
            api_ecl_flag: number[];

            /** 味方被ダメージ　庇われると+0.1 */
            api_fdam: number[];

            /** 敵被ダメージ */
            api_edam: number[];
        };

        /** 随伴護衛艦隊の航空攻撃 */
        api_stage3_combined: {

            /** 味方被雷撃フラグ */
            api_frai_flag: number[];

            /** 味方被爆撃フラグ */
            api_fbak_flag: number[];

            /** 味方クリティカルフラグ? */
            api_fcl_flag: number[];

            /** 味方被ダメージ　庇われると+0.1 */
            api_fdam: number[];
        };
    }
}