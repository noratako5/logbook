
declare module combat {

    /** 艦隊司令部情報 */
    interface BasicInfoApi {

        /** 提督固有ID */
        api_member_id: string;

        /** 提督名 */
        api_nickname: string;

        /**  */
        api_nickname_id: string;

        /**  */
        api_active_flag: number;

        /** 起動日時(≠着任日時, 今回のログイン時の時間) */
        api_starttime: number;

        /** 艦隊司令部Lv. */
        api_level: number;

        /** 階級 1から 元帥, 大将, 中将, 少将, 大佐, 中佐, 新米中佐, 少佐, 中堅少佐, 新米少佐 */
        api_rank: number;

        /** 提督経験値 */
        api_experience: number;

        /**  */
        api_fleetname: any;

        /** 提督コメント */
        api_comment: number;

        /**  */
        api_comment_id: number;

        /** 最大保有可能艦娘数 */
        api_max_chara: number;

        /** 最大保有可能装備数 */
        api_max_slotitem: number;

        /**  */
        api_max_kagu: number;

        /**  */
        api_playtime: number;

        /**  */
        api_tutorial: number;

        /** 設置している家具ID */
        api_furniture: number[];

        /** 保有艦隊数 */
        api_count_deck: number;

        /** 工廠ドック数 */
        api_count_kdock: number;

        /** 入渠ドック数 */
        api_count_ndock: number;

        /** 家具コイン数 */
        api_fcoin: number;

        /** 出撃勝利回数 */
        api_st_win: number;

        /** 出撃敗北回数 */
        api_st_lose: number;

        /** 遠征回数 */
        api_ms_count: number;

        /** 遠征成功回数 */
        api_ms_success: number;

        /** 演習勝利回数 */
        api_pt_win: number;

        /** 演習敗北回数 */
        api_pt_lose: number;

        /**  */
        api_pt_challenged: number;

        /**  */
        api_pt_challenged_win: number;

        /**  */
        api_firstflag: number;

        /** チュートリアル進捗？ */
        api_tutorial_progress: number;

        /**  */
        api_pvp: number[];

        /** 甲種勲章保有数 */
        api_medals: number;

        /** 大型艦建造可否 */
        api_large_dock: any;
    }
}