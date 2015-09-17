
declare module Packages.logbook.dto {

    /**
     * １回の会敵情報
     * @author Nekopanda
     */
    class BattleExDto {
        
        /**
         * 連合艦隊か？
         * @return
         */
        isCombined(): boolean;
    
        /**
         * 最後に行ったフェーズを取得
         * @return
         */
        getLastPhase(): BattleExDto.Phase;
    
        /**
         * 最初のフェーズを取得
         * @return
         */
        getPhase1(): BattleExDto.Phase;
    
        /**
         * ２番目のフェーズ（ない時はnull）
         * @return
         */
        getPhase2(): BattleExDto.Phase;
    
        /**
         * 戦闘結果も含んでいるか
         * これがfalseに場合は正常に記録されない
         * @return
         */
        isCompleteResult(): boolean;
    
        /**
         * 演習か？
         * @return
         */
        isPractice(): boolean;
    
        /**
         * 交戦後の味方艦HP（連合艦隊の時は第一艦隊）
         * @return
         */
        getNowFriendHp(): JavaArray<number>;
    
        /**
         * 交戦後の味方艦HP（連合艦隊でないときはnull）
         * @return
         */
        getNowFriendHpCombined(): JavaArray<number>;
    
        /**
         * 交戦後の敵艦HP
         * @return
         */
        getNowEnemyHp(): JavaArray<number>;
    
        /**
         * 味方艦隊（連合艦隊の時は第一艦隊）
         * @return
         */
        getDock(): DockDto;
    
        /**
         * 連合艦隊第二艦隊（連合艦隊でないときはnull）
         * @return
         */
        getDockCombined(): DockDto;
    
        /**
         * 戦闘のあった日時
         * @return battleDate
         */
        getBattleDate(): Packages.java.util.Date;
    
        /**
         * 味方艦隊
         * @return friends
         */
        getFriends(): Packages.java.util.List<DockDto>;
    
        /**
         * 敵艦
         * @return enemy
         */
        getEnemy(): Packages.java.util.List<EnemyShipDto>;
    
        /**
         * 味方艦のMaxHP
         * 連合艦隊の時は第一艦隊のみ
         * @return maxFriendHp
         */
        getMaxFriendHp(): JavaArray<number>;
    
        /**
         * 味方連合艦隊第二艦隊のMaxHP
         * @return maxFriendHpCombined
         */
        getMaxFriendHpCombined(): JavaArray<number>;
    
        /**
         * 敵艦のMaxHP
         * @return maxEnemyHp
         */
        getMaxEnemyHp(): JavaArray<number>;
    
        /**
         * 戦闘開始時の味方艦のHP
         * 連合艦隊の時は第一艦隊のみ
         * @return startFriendHp
         */
        getStartFriendHp(): JavaArray<number>;
    
        /**
         * 味方連合艦隊第二艦隊の戦闘開始時HP
         * @return startFriendHpCombined
         */
        getStartFriendHpCombined(): JavaArray<number>;
    
        /**
         * 敵艦の戦闘開始時HP
         * @return startEnemyHp
         */
        getStartEnemyHp(): JavaArray<number>;
    
        /**
         * 味方戦果ゲージの最大（味方艦MaxHPの合計）
         * @return friendGaugeMax
         */
        getFriendGaugeMax(): number;
    
        /**
         * 敵戦果ゲージの最大（敵艦MaxHPの合計）
         * @return enemyGaugeMax
         */
        getEnemyGaugeMax(): number;
    
        /**
         * 陣形 [味方, 敵] 
         * @return formation
         */
        getFormation(): JavaArray<string>;
    
        /**
         * 同航戦、反航戦など
         * @return formationMatch
         */
        getFormationMatch(): string;
    
        /**
         * 索敵状況 [味方, 敵]
         * @return sakuteki
         */
        getSakuteki(): string;
    
        /**
         * 出撃海域情報
         * @return questName
         */
        getQuestName(): string;
    
        /**
         * 戦闘結果のランク
         * @return rank
         */
        getRank(): ResultRank;
    
        /**
         * 戦闘のあったマスの情報
         * @return mapCelldto
         */
        getMapCellDto(): MapCellDto;
    
        /**
         * 敵艦隊の名前
         * @return enemyName
         */
        getEnemyName(): string;
    
        /**
         * ドロップ艦があったか？
         * @return dropShip
         */
        getDropShip(): boolean;
    
        /**
         * ドロップアイテムがあったか？
         * @return dropItem
         */
        isDropItem(): boolean;
    
        /**
         * ドロップ艦の艦種（アイテムの場合は「アイテム」）
         * @return dropType
         */
        isDropType(): string;
    
        /**
         * ドロップ艦・アイテムの名前
         * @return dropName
         */
        isDropName(): string;
    
        /**
         * 戦闘フェーズ（昼戦・夜戦）リスト
         * @return phaseList
         */
        getPhaseList(): Packages.java.util.List<BattleExDto.Phase>;
    
        /**
         * MVP艦が何番目の艦か (0～)
         * MVPがいない時は-1
         * @return mvp
         */
        getMvp(): number;
    
        /**
         * 連合艦隊第二艦隊のMVP艦が何番目の艦か
         * 連合艦隊でない時またはMVPがいない時は-1
         * @return mvpCombined
         */
        getMvpCombined(): number;
    
        /**
         * 司令部Lv
         * @return hqLv
         */
        getHqLv(): number;
    
        /*** 
         * BattleExDtoのバージョン
         * exVersion == 0 : Tag 34以降がない
         * exVersion == 1 : Tag 36まである
         * exVersion == 2 : Jsonがある
         * @return exVersion
         */
        getExVersion(): number;
    
        /**
         * 母港の艦娘空き枠
         * @return shipSpace
         */
        getShipSpace(): number;
    
        /**
         * 母港の装備アイテム空き枠
         * @return itemSpace
         */
        getItemSpace(): number;
    
        /**
         * 連合艦隊における退避意見 [退避する艦(0-11), 護衛艦(0-11)]
         * @return escapeInfo
         */
        getEscapeInfo(): JavaArray<number>;
    
        /**
         * 護衛退避で戦線離脱したか [第1艦隊1番艦～第2艦隊6番艦]
         * 艦隊の艦数に関係なく常に長さは12
         * @return escaped
         */
        getEscaped(): JavaArray<boolean>;

        /**
         * 戦闘結果のレスポンスJSON
         * @return resultJson
         */
        getResultJson(): Packages.javax.json.JsonObject;
    
        /**
         * @return lostflag
         */
        getLostflag(): JavaArray<boolean>;
    
        /**
         * @return shipId
         */
        getDropShipId(): number;
    }
}