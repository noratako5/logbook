
declare module kcsapi {

    /** 装備データ */
    interface ItemInfoApi {

        /** 装備ID */
        api_id: number;

        /** 並べ替え順 */
        api_sortno: number;

        /** 装備名 */
        api_name: string;

        /**
         * 装備タイプ
         *
         * [0]：大分類
         * 1 = 砲
         * 2 = 魚雷
         * 3 = 艦載機
         * 4 = 機銃・特殊弾(対空系)
         * 5 = 偵察機・電探(索敵系)
         * 6 = 強化
         * 7 = 対潜装備
         * 8 = 大発動艇・探照灯
         * 9 = 簡易輸送部材
         * 10 = 艦艇修理施設
         * 11 = 照明弾
         * 12 = 司令部施設
         * 13 = 航空要員
         * 14 = 高射装置
         * 15 = 対地装備
         * 16 = 水上艦要員
         * 17 = 大型飛行艇
         *
         * [1]：図鑑表示
         * 1 = Primary Armament
         * 2 = Secondary Armament
         * 3 = Torpedo
         * 4 = Midget Submarine
         * 5 = Carrier - Based Aircraft
         * 6 = AA Gun
         * 7 = Reconnaissance
         * 8 = Radar
         * 9 = Upgrades
         * 10 = Sonar
         * 14 = Daihatsu
         * 15 = Autogyro
         * 16 = AntiSubmarine Patrol
         * 17 = Extension Armor
         * 18 = Searchlight
         * 19 = Supply
         * 20 = Machine Tools
         * 21 = Flare
         * 22 = Fleet Command
         * 23 = Maintenance Team
         * 24 = AA Director
         * 25 = AP Shell
         * 26 = Rocket Artillery
         * 27 = Picket Crew
         * 28 = AA Shell
         * 29 = AA Rocket
         * 30 = Damage Control
         * 31 = Engine Upgrades
         * 32 = Depth Charge
         * 33 = Flying Boat
         *
         * 1 = 主砲
         * 2 = 副砲
         * 3 = 魚雷
         * 4 = 特殊潜航艇
         * 5 = 艦上機
         * 6 = 対空機銃
         * 7 = 偵察機
         * 8 = 電探
         * 9 = 強化
         * 10 = ソナー
         * 14 = 上陸用舟艇
         * 15 = オートジャイロ
         * 16 = 対潜哨戒機
         * 17 = 追加装甲
         * 18 = 探照灯
         * 19 = 簡易輸送部材
         * 20 = 艦艇修理施設
         * 21 = 照明弾
         * 22 = 司令部施設
         * 23 = 航空要員
         * 24 = 高射装置
         * 25 = 対艦強化弾
         * 26 = 対地装備
         * 27 = 水上艦要員
         * 28 = 対空強化弾
         * 29 = 対空ロケットランチャー
         * 30 = 応急修理要員
         * 31 = 機関部強化
         * 32 = 爆雷
         * 33 = 大型飛行艇
         *
         * [2]：カテゴリ
         *     (api_mst_slotitem_equiptype を参照)
         * 1 = 小口径主砲
         * 2 = 中口径主砲
         * 3 = 大口径主砲
         * 4 = 副砲
         * 5 = 魚雷
         * 6 = 艦上戦闘機
         * 7 = 艦上爆撃機
         * 8 = 艦上攻撃機
         * 9 = 艦上偵察機
         * 10 = 水上偵察機
         * 11 = 水上爆撃機
         * 12 = 小型電探
         * 13 = 大型電探
         * 14 = ソナー
         * 15 = 爆雷
         * 16 = 追加装甲
         * 17 = 機関部強化
         * 18 = 対空強化弾
         * 19 = 対艦強化弾
         * 20 = VT信管
         * 21 = 対空機銃
         * 22 = 特殊潜航艇
         * 23 = 応急修理要員
         * 24 = 上陸用舟艇
         * 25 = オートジャイロ
         * 26 = 対潜哨戒機
         * 27 = 追加装甲(中型)
         * 28 = 追加装甲(大型)
         * 29 = 探照灯
         * 30 = 簡易輸送部材
         * 31 = 艦艇修理施設
         * 32 = 潜水艦魚雷
         * 33 = 照明弾
         * 34 = 司令部施設
         * 35 = 航空要員
         * 36 = 高射装置
         * 37 = 対地装備
         * 38 = 大口径主砲(II)
         * 39 = 水上艦要員
         * 40 = 大型ソナー
         * 41 = 大型飛行艇
         *
         * [3]：アイコンID
         * 1 = 小口径主砲
         * 2 = 中口径主砲
         * 3 = 大口径主砲
         * 4 = 副砲
         * 5 = 魚雷
         * 6 = 艦上戦闘機
         * 7 = 艦上爆撃機
         * 8 = 艦上攻撃機
         * 9 = 艦上偵察機
         * 10 = 水上機
         * 11 = 電探
         * 12 = 対空強化弾
         * 13 = 対艦強化弾
         * 14 = 応急修理要員
         * 15 = 対空機銃
         * 16 = 高角砲
         * 17 = 爆雷
         * 18 = ソナー
         * 19 = 機関部強化
         * 20 = 上陸用舟艇
         * 21 = オートジャイロ
         * 22 = 対潜哨戒機
         * 23 = 追加装甲
         * 24 = 探照灯
         * 25 = 簡易輸送部材
         * 26 = 艦艇修理施設
         * 27 = 照明弾
         * 28 = 司令部施設
         * 29 = 航空要員
         * 30 = 高射装置
         * 31 = 対地装備
         * 32 = 水上艦要員
         * 33 = 大型飛行艇
         */
        api_type: number[];


        /** 耐久(0) */
        api_taik: number;

        /** 装甲 */
        api_souk: number;

        /** 火力 */
        api_houg: number;

        /** 雷装 */
        api_raig: number;

        /** 速力 */
        api_soku: number;

        /** 爆装 */
        api_baku: number;

        /** 対空 */
        api_tyku: number;

        /** 対潜 */
        api_tais: number;

        /** (0) */
        api_atap: number;

        /** 命中 */
        api_houm: number;

        /** 雷撃命中(0) */
        api_raim: number;

        /** 回避 */
        api_houk: number;

        /** 雷撃回避(0) */
        api_raik: number;

        /** 爆撃回避(0) */
        api_bakk: number;

        /** 索敵 */
        api_saku: number;

        /** 索敵妨害(0) */
        api_sakb: number;

        /** 運(0) */
        api_luck: number;

        /** 射程 */
        api_leng: number;

        /** レアリティ */
        api_rare: number;

        /** 廃棄資材 */
        api_broken: number[];

        /** 図鑑情報 */
        api_info: string;

        /**  */
        api_usebull: number;
    }
}