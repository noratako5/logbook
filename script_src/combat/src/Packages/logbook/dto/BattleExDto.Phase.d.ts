
declare module Packages.logbook.dto.BattleExDto {

    /**
     * 戦闘1フェーズの情報
     * @author Nekopanda
     */
    class Phase {

        /**
         * 連合艦隊か？
         * @return
         */
        isCombined(): boolean;
        
        /**
         * 航空戦情報 [１回目, 2回目]
         * 2回目は連合艦隊航空戦マスでの戦闘のみ
         * @return
         */
        getAirBattleDto(): JavaArray<AirBattleDto>
        
        /**
         * 攻撃の全シーケンスを取得
         * [ 航空戦1, 支援艦隊の攻撃, 航空戦2, 開幕, 夜戦, 砲撃戦1, 雷撃, 砲撃戦2, 砲撃戦3 ]
         * 各戦闘がない場合はnullになる
         * @return
         */
        getAtackSequence(): JavaArray<JavaArray<BattleAtackDto>>;
        
        /**
         * 戦闘ランクの計算に使われた情報の概要を取得
         * @param battle
         * @return
         */
        getRankCalcInfo(battleExDto: BattleExDto): string;

        /**
         * 受け取ったJSON
         * @return
         */
        getJson(): Packages.javax.json.JsonObject;
        
        /**
         * この戦闘フェーズのAPIリクエスト先
         * @return
         */
        getApi(): string;
        
        /**
         * この戦闘フェーズの種別
         * @return kind
         */
        getKind(): BattlePhaseKind;
        
        
        /**
         * この戦闘フェーズ後の味方艦HP（連合艦隊の時は第一艦隊）
         * @return nowFriendHp
         */
        getNowFriendHp(): JavaArray<number>;
        
        /**
         * この戦闘フェーズ後の味方艦HP（連合艦隊でないときはnull）
         * @return nowFriendHpCombined
         */
        getNowFriendHpCombined(): JavaArray<number>;
        
        /**
         * この戦闘フェーズ後の敵艦HP
         * @return nowEnemyHp
         */
        getNowEnemyHp(): JavaArray<number>;
        
        /**
         * この戦闘フェーズ後のランク（予測値）
         * @return estimatedRank
         */
        getEstimatedRank(): ResultRank;
        
        /**
         * この戦闘フェーズが夜戦か？
         * @return isNight
         */
        isNight(): boolean;
        
        /**
         * 支援攻撃のタイプ
         * @return supportType
         */
        getSupportType(): string;
        
        /**
         * 触接機 [味方・敵] -1の場合は「触接なし」
         * @return touchPlane
         */
        getTouchPlane(): JavaArray<number>;
        
        /**
         * 制空状態
         * @return seiku
         */
        getSeiku(): string;
        
        /**
         * 損害率 [味方, 敵]
         * @return damageRate
         */
        getDamageRate(): JavaArray<number>;
        
        /**
         * 航空戦1
         * @return air
         */
        getAir(): AirBattleDto;
        
        /**
         * 航空戦2
         * @return air2
         */
        getAir2(): AirBattleDto;
        
        /**
         * 支援艦隊の攻撃
         * @return support
         */
        getSupport(): Packages.java.util.List<BattleAtackDto>;
        
        /**
         * 開幕
         * @return opening
         */
        getOpening(): Packages.java.util.List<BattleAtackDto>;
        
        /**
         * 雷撃戦
         * @return raigeki
         */
        getRaigeki(): Packages.java.util.List<BattleAtackDto>;
        
        /**
         * 夜戦
         * @return hougeki
         */
        getHougeki(): Packages.java.util.List<BattleAtackDto>;
        
        /**
         * 砲撃戦1
         * @return hougeki1
         */
        getHougeki1(): Packages.java.util.List<BattleAtackDto>;
        
        /**
         * 砲撃戦2
         * @return hougeki2
         */
        getHougeki2(): Packages.java.util.List<BattleAtackDto>;
        
        /**
         * 砲撃戦3
         * @return hougeki3
         */
        getHougeki3(): Packages.java.util.List<BattleAtackDto>;
    }
}