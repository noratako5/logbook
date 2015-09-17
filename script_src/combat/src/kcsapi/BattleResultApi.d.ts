
declare module kcsapi {

    /** 戦闘結果 */
    interface BattleResultApi {

        /** 敵艦船ID */
        api_ship_id: number[];

        /** 勝利ランク */
        api_win_rank: string;

        /** 獲得提督経験値 */
        api_get_exp: number;

        /** MVP　1から始まる, -1=なし */
        api_mvp: number;

        /** 艦隊司令部Lv. */
        api_member_lv: number;

        /** 提督経験値 */
        api_member_exp: number;

        /** 獲得基本経験値 */
        api_get_base_exp: number;

        /** 各艦の獲得経験値 */
        api_get_ship_exp: number[];

        /** 各艦の[0]獲得前経験値, [1]次のレベルの経験値(Lv99 | 150の場合存在せず), (レベルアップしたなら)[2]その次のレベルの経験値, ... */
        api_get_exp_lvup: number[][];

        /** 敵艦撃沈数？ */
        api_dests: number;

        /** 旗艦撃沈フラグ？ */
        api_destsf: number;

        /** 味方艦撃沈フラグ？-1から始まる */
        api_lost_flag: number[];

        /** 出撃海域名 */
        api_quest_name: string;

        /** 難易度？ */
        api_quest_level: string;

        /** 敵艦隊情報 */
        api_enemy_info: {

            /** "" */
            api_user_name: string;

            /** 敵艦隊司令部Lv. */
            api_level: number | string;

            /** 敵階級　文字列 */
            api_rank: string;

            /** 敵艦隊名 */
            api_deck_name: string;
        };

        /** 初回クリアフラグ(EO海域攻略時も1) */
        api_first_clear: number;

        /** 入手フラグ [0]=アイテム, [1]=艦娘 */
        api_get_flag: number[];

        /** ドロップアイテム情報 api_get_flag[0]=1のときのみ存在 */
        api_get_useitem: {

            /** アイテムID 60=プレゼント箱 */
            api_useitem_id: number;

            /** "" */
            api_useitem_name: string;
        };

        /** ドロップ艦情報　api_get_flag[1]=1のときのみ存在 */
        api_get_ship: {

            /** 艦船ID */
            api_ship_id: number;

            /** 艦種 */
            api_ship_type: string;

            /** 艦船名 */
            api_ship_name: string;

            /** メッセージ */
            api_ship_getmes: string;
        };

        /** ドロップ装備情報？ api_get_flag[2]=1のときのみ存在 */
        api_get_slotitem: {

            /** 装備ID */
            api_slotitem_id: number;
        };

        /** 海域攻略報酬　イベント海域突破時のみ存在 */
        api_get_eventitem: {

            /** 報酬種別 1=アイテム, 2=艦娘, 3=装備 */
            api_type: number;

            /** ID */
            api_id: number;

            /** 個数？ */
            api_value: number;
        }[];

        /** ？ api_get_eventitem存在時に消滅 */
        api_get_eventflag: number;


        /** EO海域攻略時：獲得戦果(文字列) それ以外は0(数値) */
        api_get_exmap_rate: string | number;


        /** 取得アイテムID(文字列) "57"=勲章　それ以外は0(数値) */
        api_get_exmap_useitem_id: string | number;
    }
}