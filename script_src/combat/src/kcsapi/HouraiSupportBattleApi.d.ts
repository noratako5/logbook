
declare module kcsapi {

    interface HouraiSupportBattleApi extends SupportInfoApi {

        /** 艦隊ID */
        api_dock_id: number | string;

        /** 艦隊ID */
        api_deck_id: number | string;

        /** 艦船固有ID */
        api_ship_id: number[];

        /** 中破グラフィックフラグ 0= 通常, 1 = 中破 なお適用はバナーのみで、旗艦グラフィックは変わらない */
        api_undressing_flag: number[];

        /** クリティカル判定　0=ミス, 1=命中, 2=クリティカル　-1から */
        api_cl_list: number[];

        /** 敵被ダメージ　-1から */
        api_damage: number[];
    }
}