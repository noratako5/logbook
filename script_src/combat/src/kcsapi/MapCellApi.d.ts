
declare module kcsapi {

    /** 進撃 */
    interface MapCellApi {

        /** 羅針盤を表示するかどうか */
        api_rashin_flg: number;

        /** 妖精さん　0= なし, 1 = 眠そう, 2 = 桃の髪飾り, 3 = ひよこ, 4 = 魔法使い */
        api_rashin_id: number;

        /** 海域カテゴリID(2 - 3でいう2) */
        api_maparea_id: number;

        /** 海域カテゴリ内番号(2 - 3でいう3) */
        api_mapinfo_no: number;

        /** 次に向かうセルのID */
        api_no: number;

        /** 次のセルの色 ? 　3 = 青, 4 = 赤 */
        api_color_no: number;

        /** イベント種別　0= 非戦闘セル 1= 通常戦闘 2= 夜戦 3= 夜昼戦 4= 航空戦 */
        api_event_kind: number;

        /**  6(気のせいだった)時は 0= "気のせいだった。" 1= "敵影を見ず。" 2= 能動分岐 */
        api_event_id : number;

        /** 次のマスでのルート分岐の本数 0= 行き止まり */
        api_next: number;

        /** ボスセルID */
        api_bosscell_no: number;

        /** ボス到達済みフラグ？コード中にスペルミスがあり未使用 */
        api_bosscomp: number;

        /** 0 = なし 1= <敵艦隊発見!> 2=<攻撃目標発見!>?　startにはないので注意 */
        api_comment_kind: number;

        /** 0 = なし 1= 索敵機発艦　startにはないので注意 */
        api_production_kind: number;

        /** イベント海域ゲージ情報 */
        api_eventmap: {

            /** ゲージ最大値 */
            api_max_maphp: number;

            /** ゲージ現在値　startとは異なり実際の値が入る */
            api_now_maphp: number;

            /** ダメージ倍率？ */
            api_dmg: number;
        };

        /** 次が戦闘マスのときのみ存在 */
        api_enemy: {

            /** 敵艦隊ID */
            api_enemy_id: number;

            /**  */
            api_result: number;

            /** "start war" */
            api_result_str: number;
        };

        /** 次が資源マスのときのみ存在 */
        api_itemget: {

            /** 4=資源, 11=家具箱(中), 12=家具箱(大)  */
            api_usemst: number;

            /** 1=燃料, 2=弾薬, 11=家具箱(中), 12=家具箱(大) */
            api_id: number;

            /** 入手資源量 */
            api_getcount: number;

            /** アイテム名(家具箱のみ?燃料等の資源は"") */
            api_name: string;

            /** 1=燃料, 2=弾薬, 11=家具箱(中), 12=家具箱(大) */
            api_icon_id: number;
        };

        /** 次が渦潮マスのときのみ存在 */
        api_happening: {

            /** 1 */
            api_type: number;

            /** 落とした資源の量 */
            api_count: number;

            /** 4 */
            api_usemst: number;

            /** 落とした資源の種類　2=弾薬 */
            api_mst_id: number;

            /** 表示アイコンID？　2=弾薬 */
            api_icon_id: number;

            /** 電探による被害軽減フラグ(1=軽減) */
            api_dentan: number;
        };

        /** 獲得戦果　次が船団護衛成功マスのときのみ存在(ゲージ破壊時追加で) */
        api_get_eo_rate: number;

        /** 次が船団護衛成功マスのときのみ存在(ゲージ破壊時追加で) */
        api_itemget_eo_result: {

            /** 5 */
            api_usemst: number;

            /** 60=プレゼント箱 */
            api_id: number;

            /** 入手個数 */
            api_getcount: number;
        };

        /** 次が船団護衛成功マスのときのみ存在 */
        api_itemget_eo_comment: {

            /** 4=資源 */
            api_usemst: number;

            /** 多分api_itemgetと同じ(3=鋼材) */
            api_id: number;

            /** 入手資源量 */
            api_getcount: number;
        };

        /** 次が能動分岐マスのときのみ存在 */
        api_select_route: {

            /** 選択可能なセルIDのリスト */
            api_select_cells: number[];
        };
    }
}