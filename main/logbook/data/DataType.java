/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.data;

/**
 * データが何を示すのかを列挙する
 *
 */
public enum DataType {

    /** フィルタ前のデータ */
    UNDEFINED(null),
    /** 保有艦 */
    SHIP2("/kcsapi/api_get_member/ship2"),
    /** 遠征 */
    DECK_PORT("/kcsapi/api_get_member/deck_port"),
    /** 入渠 */
    NDOCK("/kcsapi/api_get_member/ndock"),
    /** アイテム一覧 */
    SLOTITEM_MEMBER("/kcsapi/api_get_member/slotitem"),
    /** アイテム一覧 */
    SLOTITEM_MASTER("/kcsapi/api_get_master/slotitem"),
    /** 戦闘 */
    BATTLE("/kcsapi/api_req_sortie/battle"),
    /** 戦闘(夜戦) */
    BATTLE_MIDNIGHT("/kcsapi/api_req_battle_midnight/battle"),
    /** 戦闘結果 */
    BATTLERESULT("/kcsapi/api_req_sortie/battleresult"),
    /** 開発 */
    CREATEITEM("/kcsapi/api_req_kousyou/createitem"),
    /** 建造 */
    CREATESHIP("/kcsapi/api_req_kousyou/createship"),
    /** 建造(入手) */
    GETSHIP("/kcsapi/api_req_kousyou/getship");

    private final String url;

    private DataType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
