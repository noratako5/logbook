
declare module kcsapi {

    /** 装備品情報 */
    interface ItemApi {

        /** 装備固有ID */
        api_id: number;

        /** 装備ID */
        api_slotitem_id: number;

        /** ロック有無 */
        api_locked: number;

        /** 改修Level */
        api_level: number;

        /** 艦載機熟練度　1以上の時のみ存在 */
        api_alv: number;
    }
}