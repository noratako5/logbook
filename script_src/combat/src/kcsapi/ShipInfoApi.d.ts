
declare module kcsapi {

    /** 艦船データ */
    interface ShipInfoApi {

        /** 艦娘固有ID */
        api_id: number;

        /** 図鑑番号 */
        api_sortno: number;

        /** 艦娘名 */
        api_name: string;

        /** 艦娘名読み / (敵艦船)クラス */
        api_yomi: string;

        /** 艦種 */
        api_stype: number;

        /** 改装Lv */
        api_afterlv: number;

        /** 改装後ID 文字列 "0"= なし */
        api_aftershipid: string;

        /** 耐久 */
        api_taik: number[];

        /** 装甲 */
        api_souk: number[];

        /** 火力 */
        api_houg: number[];

        /** 雷装 */
        api_raig: number[];

        /** 対空 */
        api_tyku: number[];

        /** 運 */
        api_luck: number[];

        /** 速力　0= 陸上基地, 5 = 低速, 10 = 高速 */
        api_soku: number;

        /** 射程 */
        api_leng: number;

        /** スロット数 */
        api_slot_num: number;

        /** 艦載機搭載数 */
        api_maxeq: number[];

        /** 建造時間 */
        api_buildtime: number;

        /** 解体資材 */
        api_broken: number[];

        /** 近代化改修強化値 */
        api_powup: number[];

        /** レアリティ */
        api_backs: number[];

        /** 取得時台詞 */
        api_getmes: string;

        /** 改装鋼材 */
        api_afterfuel: number;

        /** 改装弾薬 */
        api_afterbull: number;

        /** 消費燃料 */
        api_fuel_max: number;

        /** 消費弾薬 */
        api_bull_max: number;

        /** ボイス設定フラグ ビットフラグ; 1 = 放置ボイス, 2 = 時報 */
        api_voicef: number;
    }
}