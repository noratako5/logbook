
declare module kcsapi {

    interface NightPhaseApi {

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

        /** 陣形　昼戦のそれと同じ(但し味方陣形は文字列型) */
        api_formation: (number | string)[];

        /** 夜間触接装備ID[0] = 味方, [1] = 敵 */
        api_touch_plane: number[];

        /** 照明弾発射艦[0] = 味方, [1] = 敵 */
        api_flare_pos: number[];

        /** 夜間砲撃戦 */
        api_hougeki: NightHougekiBattleApi;
    }
}