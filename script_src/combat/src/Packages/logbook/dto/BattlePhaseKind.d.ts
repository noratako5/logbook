
declare module Packages.logbook.dto {

    /**
     * 戦闘フェーズの種別
     * @author Nekopanda
     */
    class BattlePhaseKind {

        /** 通常の昼戦 */
        static BATTLE: BattlePhaseKind;

        /** 通常の夜戦 */
        static MIDNIGHT: BattlePhaseKind;

        /** 演習の昼戦 */
        static PRACTICE_BATTLE: BattlePhaseKind;

        /** 演習の夜戦 */
        static PRACTICE_MIDNIGHT: BattlePhaseKind;

        /** 夜戦マスの戦闘 */
        static SP_MIDNIGHT: BattlePhaseKind;
        
        /** 夜戦→昼戦マスの昼戦 */
        static NIGHT_TO_DAY: BattlePhaseKind;

        /** 夜戦→昼戦マスの昼戦 */
        static AIR_BATTLE: BattlePhaseKind;

        /** 連合艦隊空母機動部隊の昼戦 */
        static COMBINED_BATTLE: BattlePhaseKind;

        /** 連合艦隊航空戦マス */
        static COMBINED_AIR: BattlePhaseKind;

        /** 連合艦隊の夜戦 */
        static COMBINED_MIDNIGHT: BattlePhaseKind;

        /** 連合艦隊での夜戦マスの戦闘 */
        static COMBINED_SP_MIDNIGHT: BattlePhaseKind;

        /** 連合艦隊水上打撃部隊の昼戦 */
        static COMBINED_BATTLE_WATER: BattlePhaseKind;

        /**
         * 夜戦か？
         * @return night
         */
        isNight(): boolean;

        /**
         * 開幕戦は第二艦隊が行うか？
         * @return
         */
        isOpeningSecond(): boolean;

        /**
         * 夜戦は第二艦隊が行うか？
         * @return
         */
        isHougekiSecond(): boolean;

        /**
         * 砲撃戦1は第二艦隊が行うか？
         * @return
         */
        isHougeki1Second(): boolean;

        /**
         * 砲撃戦2は第二艦隊が行うか？
         * @return
         */
        isHougeki2Second(): boolean;

        /**
         * 砲撃戦3は第二艦隊が行うか？
         * @return
         */
        isHougeki3Second(): boolean;

        /**
         * 雷撃戦は第二艦隊が行うか？
         * @return
         */
        isRaigekiSecond(): boolean;

        /**
         * この戦闘のAPIリクエスト先
         * @return api
         */
        getApi(): Packages.logbook.data.DataType;

        /**
         * @return practice
         */
        isPractice(): boolean;
    }
}