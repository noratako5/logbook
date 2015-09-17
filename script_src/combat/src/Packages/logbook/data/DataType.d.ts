
declare module Packages.logbook.data {

    /**
     * データが何を示すのかを列挙する
     *
     */
    class DataType {

        /** 補給 */
        static CHARGE: DataType;

        /** 編成 */
        static CHANGE: DataType;

        /** 母港 */
        static PORT: DataType;

        /** 保有艦 */
        static SHIP2: DataType;

        /** 保有艦 */
        static SHIP3: DataType;

        /** 出撃中の更新 */
        static SHIP_DECK: DataType;

        /** 遠征(帰還) */
        static MISSION_RESULT: DataType;

        /** 基本 */
        static BASIC: DataType;

        /** 資材 */
        static MATERIAL: DataType;

        /** 入渠ドック */
        static NDOCK: DataType;

        /** アイテム一覧 */
        static SLOTITEM_MEMBER: DataType;

        /** 艦隊 */
        static DECK: DataType;

        /** 戦闘 */
        static BATTLE: DataType;

        /** 戦闘(夜戦) */
        static BATTLE_MIDNIGHT: DataType;

        /** 戦闘(夜戦) */
        static BATTLE_SP_MIDNIGHT: DataType;

        /** 戦闘(夜戦→昼戦) */
        static BATTLE_NIGHT_TO_DAY: DataType;

        /** 戦闘(航空戦) */
        static AIR_BATTLE: DataType;

        /** 戦闘(航空戦) */
        static COMBINED_AIR_BATTLE: DataType;

        /** 戦闘 */
        static COMBINED_BATTLE: DataType;

        /** 戦闘 */
        static COMBINED_BATTLE_MIDNIGHT: DataType;

        /** 戦闘 */
        static COMBINED_BATTLE_SP_MIDNIGHT: DataType;

        /** 戦闘 */
        static COMBINED_BATTLE_WATER: DataType;

        /** 戦闘結果 */
        static BATTLE_RESULT: DataType;

        /** 戦闘結果(連合艦隊) */
        static COMBINED_BATTLE_RESULT: DataType;

        /** 退避 */
        static COMBINED_BATTLE_GOBACK_PORT: DataType;

        /** 開発 */
        static CREATE_ITEM: DataType;

        /** 建造 */
        static CREATE_SHIP: DataType;

        /** 建造ドック */
        static KDOCK: DataType;

        /** 建造(入手) */
        static GET_SHIP: DataType;

        /** 解体 */
        static DESTROY_SHIP: DataType;

        /** 廃棄 */
        static DESTROY_ITEM2: DataType;

        /** 近代化改修 */
        static POWERUP: DataType;

        /** 艦娘のロック操作 */
        static LOCK_SHIP: DataType;

        /** 装備ロック操作 */
        static LOCK_SLOTITEM: DataType;

        /** 装備改修 */
        static REMODEL_SLOT: DataType;

        /** 出撃 */
        static START: DataType;

        /** 進撃 */
        static NEXT: DataType;

        /** 任務一覧 */
        static QUEST_LIST: DataType;

        /** 任務消化 */
        static QUEST_CLEAR: DataType;

        /** 設定 */
        static START2: DataType;

        /** マップ情報 */
        static MAPINFO: DataType;

        /** 遠征情報 */
        static MISSION: DataType;

        /** 演習情報 */
        static PRACTICE: DataType;

        /** 演習情報 */
        static PRACTICE_ENEMYINFO: DataType;

        /** 戦闘 */
        static PRACTICE_BATTLE: DataType;

        /** 戦闘(夜戦) */
        static PRACTICE_BATTLE_MIDNIGHT: DataType;

        /** 戦闘結果 */
        static PRACTICE_BATTLE_RESULT: DataType;

        /** 連合艦隊操作 */
        static COMBINED: DataType;

        /** 入渠開始 */
        static NYUKYO_START: DataType;

        /** 高速修復 */
        static NYUKYO_SPEEDCHANGE: DataType;

        /** 改造 */
        static REMODELING: DataType;

        /** 疲労度回復アイテム使用 */
        static ITEMUSE_COND: DataType;

        /** フィルタ前のデータ */
        static UNDEFINED: DataType;

        getUrl(): string;

        getApiName(): string;
    }
}