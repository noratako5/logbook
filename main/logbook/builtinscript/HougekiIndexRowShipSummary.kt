package logbook.builtinscript

import logbook.dto.ShipBaseDto
import java.util.*

private const val NOW_SONSYO_INDEX = 5

fun HougekiIndexRowShipSummaryRowHeader(): ArrayList<String> {
    val header = ArrayList<String>()
    header.add("ID")
    header.add("名前")
    header.add("Lv")
    header.add("艦種")
    header.add("射程")
    header.add("損傷")
    //装備については砲撃時点の搭載数が取得できないので諦める
    //空母系は素手ないし全滅での砲撃不可能を考慮する必要あり
    return header
}
fun HougekiIndexRowShipSummaryRowBody(ship: ShipBaseDto?): ArrayList<String> {
    val body = ArrayList<String>()
    body.add(ship?.shipInfo?.shipId?.toString()?:"")
    body.add(ship?.shipInfo?.fullName?.toString()?:"")
    body.add(ship?.lv?.toString()?:"")
    body.add(ship?.shipInfo?.type?:"")
    body.add(when(ship?.param?.leng){0->"超短";1->"短";2->"中";3->"長";4->"超長";else->""})
    body.add("")
    return body
}

/**
 * HPと損傷欄を書き換えて自身を返す
 * cloneしないので注意
 */
fun  ArrayList<String>.updateHougekiIndexRowShipRowBody(hp: Int, maxHp: Int): ArrayList<String> {
    if(maxHp <= 0){
        return this
    }
    val hpRate = 4.0 * hp.toDouble() / maxHp.toDouble()
    this[NOW_SONSYO_INDEX] =
            if (hpRate > 3) { "小破未満" }
            else if (hpRate > 2) { "小破" }
            else if (hpRate > 1) { "中破" }
            else if (hpRate > 0) { "大破" }
            else { "轟沈" }
    return this
}