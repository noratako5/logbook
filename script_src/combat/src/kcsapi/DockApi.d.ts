
declare module kcsapi {

    /** 艦隊情報 個数はそのまま保有艦隊数と同じになる(パディング等はない) */
    interface DockApi {

        /**  */
        api_member_id: number;

        /** 艦隊番号 */
        api_id: number | string;

        /** 艦隊名 */
        api_name: string;

        /**  */
        api_name_id: string;

        /** 遠征状況    [0]={0=未出撃, 1=遠征中, 2=遠征帰投, 3=強制帰投中}, [1]=遠征先ID, [2]=帰投時間, [3]=0 */
        api_mission: number[];

        /**  */
        api_flagship: string;

        /** 所属艦船ID　空きは-1 */
        api_ship: number[];
    }
}