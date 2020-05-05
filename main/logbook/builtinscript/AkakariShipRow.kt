package logbook.builtinscript

import logbook.builtinscript.akakariLog.AkakariSyutsugekiLogReader
import logbook.dto.ShipBaseDto
import logbook.dto.ShipDto
import logbook.util.JacksonUtil
import java.util.*

private const val NOW_HP_INDEX = 6
private const val NOW_SONSYO_INDEX = 8

private var _shipRowHeader: ArrayList<String>? = null
fun AkakariShipRowHeader(): ArrayList<String> {
    if (_shipRowHeader != null) {
        return _shipRowHeader!!
    }
    val header = ArrayList<String>()
    header.add("編成順")
    header.add("ID")
    header.add("名前")
    header.add("種別")
    header.add("疲労")
    header.add("戦闘後疲労")
    header.add("残耐久")
    header.add("最大耐久")
    header.add("損傷")
    header.add("残燃料")
    header.add("戦闘後残燃料")
    header.add("最大燃料")
    header.add("残弾薬")
    header.add("戦闘後残弾薬")
    header.add("最大弾薬")
    header.add("Lv")
    header.add("速力")
    header.add("火力")
    header.add("雷装")
    header.add("対空")
    header.add("装甲")
    header.add("回避")
    header.add("対潜")
    header.add("索敵")
    header.add("運")
    header.add("射程")
    header.addAll(AkakariItemRowHeader())
    _shipRowHeader = header
    return _shipRowHeader!!
}

//戦闘中に更新されるHPと損小状態が空、女神など非対応
fun AkakariShipRowBodyBase(ship: ShipBaseDto?, maxHp: Int, index: Int,date:Date?): ArrayList<String> {
    val shipAfterBattle = (ship as? ShipDto)?.let{AkakariSyutsugekiLogReader.shipAfterBattle(date,it.id)}

    val body = ArrayList<String>()
    body.add(ship?.run{(index + 1).toString()}?:"")
    body.add(ship?.shipInfo?.shipId?.toString()?:"")
    body.add(ship?.shipInfo?.fullName?:"")
    body.add(ship?.shipInfo?.type?:"")
    body.add((ship as? ShipDto)?.cond?.toString()?:"")
    body.add(shipAfterBattle?.get("api_cond")?.let{JacksonUtil.toInt(it).toString()}?:"")
    body.add("")//現在HPは出力時に置き換える
    body.add(maxHp.toString())
    body.add("")
    body.add((ship as? ShipDto)?.fuel?.toString()?:"")
    body.add(shipAfterBattle?.get("api_fuel")?.let{JacksonUtil.toInt(it).toString()}?:"")
    body.add(ship?.shipInfo?.maxFuel?.toString()?:"")
    body.add((ship as? ShipDto)?.bull?.toString()?:"")
    body.add(shipAfterBattle?.get("api_bull")?.let{JacksonUtil.toInt(it).toString()}?:"")
    body.add(ship?.shipInfo?.maxBull?.toString()?:"")
    body.add(ship?.lv?.toString()?:"")
    val soku = (ship as? ShipDto)?.run{this.soku}?:ship?.param?.soku
    body.add((when(soku){0->"陸上";5->"低速";10->"高速";15->"高速+";20->"最速" else->""}))
    body.add(ship?.param?.houg?.toString()?:"")
    body.add(ship?.param?.raig?.toString()?:"")
    body.add(ship?.param?.taiku?.toString()?:"")
    body.add(ship?.param?.souk?.toString()?:"")
    body.add(ship?.param?.kaih?.toString()?:"")
    body.add(ship?.param?.tais?.toString()?:"")
    body.add(ship?.param?.saku?.toString()?:"")
    body.add(ship?.param?.luck?.toString()?:"")
    body.add(when(ship?.param?.leng){0->"超短";1->"短";2->"中";3->"長";4->"超長";else->""})
    body.addAll(AkakariItemRowBody(ship,date))
    return body

}

/**
 * HPと損傷欄を書き換えて自身を返す
 * cloneしないので注意
 */
fun  ArrayList<String>.updateAkakariShipRowBody(hp: Int, maxHp: Int): ArrayList<String> {
    if(maxHp <= 0){
        return this
    }
    this[NOW_HP_INDEX] = hp.toString()
    val hpRate = 4.0 * hp.toDouble() / maxHp.toDouble()
    this[NOW_SONSYO_INDEX] =
            if (hpRate > 3) { "小破未満" }
            else if (hpRate > 2) { "小破" }
            else if (hpRate > 1) { "中破" }
            else if (hpRate > 0) { "大破" }
            else { "轟沈" }
    return this
}

/**
 * HPと損傷欄を書き換えて自身を返す
 * cloneしないので注意
 */
fun ArrayList<ArrayList<String>> .setupAkakariShipRowBodyArray(hp:IntArray,maxHp:IntArray):ArrayList<ArrayList<String>>{
    for( i in this.indices){
        this[i].updateAkakariShipRowBody( hp.tryGet(i) ?: 0  , maxHp.tryGet(i) ?: 0 )
    }
    return this
}

fun AkakariShipRowInitHP(arg:ScriptArg,startHp:ArrayList<IntArray>){
    if(arg.battle.maxFriendHp != null) {
        arg.friendAkakariRows.setupAkakariShipRowBodyArray(startHp[HP_INDEX_FRIEND], arg.battle.maxFriendHp)
    }
    if(arg.battle.maxFriendHpCombined != null) {
        arg.combinedAkakariRows.setupAkakariShipRowBodyArray(startHp[HP_INDEX_FRIEND_COMBINED], arg.battle.maxFriendHpCombined)
    }
    if(arg.battle.maxEnemyHp != null) {
        arg.enemyAkakariRows.setupAkakariShipRowBodyArray(startHp[HP_INDEX_ENEMY], arg.battle.maxEnemyHp)
    }
    if(arg.battle.maxEnemyHpCombined != null) {
        arg.enemyCombinedAkakariRows.setupAkakariShipRowBodyArray(startHp[HP_INDEX_ENEMY_COMBINED], arg.battle.maxEnemyHpCombined)
    }
}