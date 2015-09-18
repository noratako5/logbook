
declare module combat {

    /** 所属艦船情報 */
    interface ShipApi {

        /** 艦船固有ID */
        api_id: number;

        /** 並び替え順？ */
        api_sortno: number;

        /** 艦船ID */
        api_ship_id: number;

        /** Lv */
        api_lv: number;

        /** 経験値　[0]=累積, [1]=次のレベルまで, [2]=経験値バー割合 */
        api_exp: number[];

        /** 現在HP */
        api_nowhp: number;

        /** 最大HP */
        api_maxhp: number;

        /** 射程 */
        api_leng: number;

        /** 装備　空きは-1 */
        api_slot: number[];

        /** 艦載機搭載数 */
        api_onslot: number[];

        /** 補強スロット 0=未解放, -1=未装備 */
        api_slot_ex: number;

        /** 近代化改修状態　[0]=火力, [1]=雷装, [2]=対空, [3]=装甲, [4]=運 */
        api_kyouka: number[];

        /** レアリティ？ */
        api_backs: number;

        /** 搭載燃料 */
        api_fuel: number;

        /** 搭載弾薬 */
        api_bull: number;

        /** スロット数 */
        api_slotnum: number;

        /** 入渠時間(ミリ秒) */
        api_ndock_time: number;

        /** 入渠時の消費資材　[0]=燃料, [1]=鋼材 */
        api_ndock_item: number[];

        /** 改装☆？ */
        api_srate: number;

        /** コンディション */
        api_cond: number;

        /** 火力　[0]=現在値(装備込み), [1]=最大値 */
        api_karyoku: number[];

        /** 雷装　なお、最大値は基本的にLv99時点の素ステータス */
        api_raisou: number[];

        /** 対空 */
        api_taiku: number[];

        /** 装甲 */
        api_soukou: number[];

        /** 回避 */
        api_kaihi: number[];

        /** 対潜 */
        api_taisen: number[];

        /** 索敵 */
        api_sakuteki: number[];

        /** 運　　[1]=最大値 */
        api_lucky: number[];

        /** 保護ロック有無 */
        api_locked: number;

        /** ロックされている装備を装備しているか */
        api_locked_equip: number;

        /** 出撃海域　イベント中のみ存在 */
        api_sally_area: number;
    }
}