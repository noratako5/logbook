
declare module Packages.logbook.internal {

    /**
     * @author Nekopanda
     *
     */
    class EnemyData {

        /**
         * @return enemyId
         */
        getEnemyId(): number;

        getEnemyName(): string;

        /**
         * @return enemyShips
         */
        getEnemyShipsId(): JavaArray<number>;

        getEnemyShips(): JavaArray<string>;

        /**
         * @return formation
         */
        getFormation(): string;
    }
}