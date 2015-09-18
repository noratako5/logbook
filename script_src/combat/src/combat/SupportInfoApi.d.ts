
declare module combat {

    interface SupportInfoApi {

        /** 空撃情報　スペルミスに注意 */
        api_support_airatack: AirSupportBattleApi;

        /** 砲雷撃情報 */
        api_support_hourai: HouraiSupportBattleApi;
    }
}