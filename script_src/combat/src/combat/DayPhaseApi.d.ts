
declare module combat {

    /** 戦闘 */
    interface DayPhaseApi {

        /** こちらの出撃艦隊ID */
        api_dock_id: number | string;

        /** こちらの出撃艦隊ID */
        api_deck_id: number | string;

        /** 敵艦船ID - 1から始まる */
        api_ship_ke: number[];

        /** 敵艦船Lv. */
        api_ship_lv: number[];

        /** 味方 / 敵艦船の現在HP - 1から始まる */
        api_nowhps: number[];

        /** 〃最大HP */
        api_maxhps: number[];

        /** 味方随伴護衛艦隊の現在HP */
        api_nowhps_combined: number[];

        /** 〃最大HP */
        api_maxhps_combined: number[];

        /** 夜戦可否フラグ */
        api_midnight_flag: number;

        /** 敵艦船装備スロット　空欄は- 1, 艦がいれば[5], いなければ[4] */
        api_eSlot: number[][];

        /** 敵艦船パラメータ強化(近代化改修) */
        api_eKyouka: number[][];

        /** 味方艦船基礎ステータス */
        api_fParam: number[][];

        /** 敵艦船基礎ステータス */
        api_eParam: number[][];

        /** 味方随伴護衛艦隊基礎ステータス */
        api_fParam_combined: number[][];

        /** 索敵　[0] = 味方, [1] = 敵    1= 成功, 2 = 失敗 ?, 5 = 失敗 */
        api_search: number[];

        /**
         * 陣形/交戦形態?　[0]=味方, [1]=敵, [2]=交戦形態
         * [0|1]：1=単縦陣 2=複縦陣, 3=輪形陣, 4=梯形陣?, 5=単横陣?, "11"-"14"=第n警戒航行序列
         * [2]：2=反航戦？
         */
        api_formation: (number | string)[];

        /** 航空戦フラグ？ */
        api_stage_flag: number[];

        /**基地航空隊攻撃　概ね航空戦データに準じる　[発動した回数]*/
        api_air_base_attack: AirBaseAttackApi[];

        /** 航空戦情報 */
        api_kouku: AirBattleApi;

        /** 支援艦隊フラグ　0=到着せず, 1=空撃?, 2=砲撃, 3=雷撃? */
        api_support_flag: number;

        /** 支援艦隊情報 */
        api_support_info: SupportInfoApi;

        /** 第二次航空戦フラグ */
        api_stage_flag2: number[];

        /** 第二次航空戦情報　api_koukuと同じ */
        api_kouku2: AirBattleApi;

        /** 開幕雷撃フラグ */
        api_opening_flag: number;

        /** 開幕雷撃戦 *スペルミスあり、注意 */
        api_opening_atack: RaigekiBattleApi;

        /** 砲雷撃戦フラグ */
        api_hourai_flag: number[];

        /** 砲撃戦1巡目(随伴護衛艦隊) */
        api_hougeki1: HougekiBattleApi;

        /** 砲撃戦2巡目(機動部隊本隊1巡目)　上に同じ */
        api_hougeki2: HougekiBattleApi;

        /** 砲撃戦3巡目(機動部隊本隊2巡目)　上に同じ */
        api_hougeki3: HougekiBattleApi;

        /** 雷撃戦　開幕雷撃戦と同じ */
        api_raigeki: RaigekiBattleApi;
    }
}