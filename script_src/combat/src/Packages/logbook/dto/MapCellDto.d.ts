
declare module Packages.logbook.dto {

    /**
     * マスの情報
     * @author Nekopanda
     */
    class MapCellDto {

        toString(detailed?: boolean, withBoss?: boolean): string;

        getReportString(): string;

        detailedString(): string;

        getAreaString(): string;

        getAreaId(): number;

        /**
         * マップ
         * 3-2-1レベリングのポイントだったら[3,2,2]
         * @return map
         */
        getMap(): JavaArray<number>;

        /**
         * エネミーID
         * @return enemyId
         */
        getEnemyId(): number;

        /**
         * 色
         * @return colorId
         */
        getColorNo(): number;

        /**
         * @return bosscellNo
         */
        getBosscellNo(): number;

        isBoss(): boolean;

        /**
         * @return enemyData
         */
        getEnemyData(): Packages.logbook.internal.EnemyData;

        /**
         * 出撃直後か？
         * @return start
         */
        isStart(): boolean;

        /**
         * @return eventId
         */
        getEventId(): number;

        /**
         * @return eventKind
         */
        getEventKind(): number;
    }
}