package logbook.builtinscript

import logbook.dto.ItemDto
import logbook.dto.ItemInfoDto
import logbook.dto.ShipBaseDto
import logbook.dto.ShipDto
import java.util.*

private var _shipSakutekiRowHeader: ArrayList<String>? = null
fun ShipSakutekiRowHeader(): ArrayList<String> {
    if (_shipSakutekiRowHeader != null) {
        return _shipSakutekiRowHeader!!
    }
    val header = ArrayList<String>()
    header.add("編成順")
    header.add("ID")
    header.add("名前")
    header.add("種別")
    header.add("Lv")
    header.add("装備込み索敵")
    header.add("素索敵")
    header.add("索敵スコア")
    header.addAll(ItemSakutekiRowHeader())
    _shipSakutekiRowHeader = header
    return _shipSakutekiRowHeader!!
}

fun ShipSakutekiRowBody(ship: ShipBaseDto?, index: Int): ArrayList<String> {
        val body = ArrayList<String>()
        body.add(ship?.run{(index + 1).toString()}?:"")
        body.add(ship?.shipInfo?.shipId?.toString()?:"")
        body.add(ship?.shipInfo?.fullName?:"")
        body.add(ship?.shipInfo?.type?:"")
        body.add(ship?.lv?.toString()?:"")
        body.add(ship?.param?.saku?.toString()?:"")
        body.add((ship as? ShipDto)?.sakutekiWithoutItem?.toString()?:"")
        body.add((ship as? ShipDto)?.sakutekiScoreWithoutItem?.Kirisute(10)?:"")
        body.addAll(ItemSakutekiRowBody(ship))
        return body
}

