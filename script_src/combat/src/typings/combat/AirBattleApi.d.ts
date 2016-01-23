
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

            /** 対空カットイン */
            api_air_fire: {

                /** 発動艦のインデックス 0から始まる */
                api_idx: number;

                /**
                 * カットイン種別
                 *  1：高角砲x2/電探
                 *  2：高角砲/電探
                 *  3：高角砲x2
                 *  4：大口径主砲/三式弾/高射装置/電探
                 *  5：高角砲+高射装置x2/電探
                 *  6：大口径主砲/三式弾/高射装置
                 *  7：高角砲/高射装置/電探
                 *  8：高角砲+高射装置/電探
                 *  9：高角砲/高射装置
                 * 10：高角砲/集中機銃/電探
                 * 11：高角砲/集中機銃
                 * 12：集中機銃/機銃/電探
                 */
                api_kind: number;

                /** 表示装備IDリスト */
                api_use_items: number[];
            }
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