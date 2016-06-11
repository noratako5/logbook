
declare module combat {

    interface AirBaseAttackApi {
        /**航空隊ID*/
        api_base_id: number;
        /**航空戦フラグ　[n]=0のときapi_stage<n>=nullになる(航空戦力なし, 艦戦のみなど)*/
        api_stage_flag: number;
        /**敵のみ　[1][n]*/
        api_plane_from: number[][];
        /**航空隊IDリスト*/
        api_squadron_plane: {
            /**装備マスターID*/
            api_mst_id: number;
            /**機数*/
            api_count: number;
        }[];
        /** 航空戦1　空対空戦闘 */
        api_stage1: {
            /**基地航空隊参加機数*/
            api_f_count: number;
            /**〃喪失機数*/
            api_f_lostcount: number;
            /**敵航空隊参加機数　偵察機も含む*/
            api_e_count: number;
            /**〃喪失機数*/
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
            /** 敵被雷撃フラグ */
            api_erai_flag: number[];
            /** 敵被爆撃フラグ */
            api_ebak_flag: number[];
            /** 敵?フラグ */
            api_ecl_flag: number[];
            /** 敵被ダメージ */
            api_edam: number[];
        };
    }
}