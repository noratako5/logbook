package logbook.builtinscript

import logbook.dto.ShipBaseDto
import logbook.dto.ShipDto
import java.util.*

private const val NOW_HP_INDEX = 5
private const val NOW_SONSYO_INDEX = 7

private var _shipRowHeader: ArrayList<String>? = null
fun AkakariSienShipAirSummaryRowHeader(): ArrayList<String> {
    if (_shipRowHeader != null) {
        return _shipRowHeader!!
    }
    val header = ArrayList<String>()
    header.add("ID")
    header.add("名前")
    header.add("種別")
    header.add("疲労")
    header.add("残耐久")
    header.add("最大耐久")
    header.add("損傷")
    header.add("Lv")
    header.add("対潜")
    header.addAll(AkakariItemRowHeader())
    _shipRowHeader = header
    return _shipRowHeader!!
}

fun AkakariSienShipAirSummaryRow(ship: ShipBaseDto?, maxHp: Int, date:Date?): ArrayList<String> {
    val body = ArrayList<String>()
    body.add(ship?.shipInfo?.shipId?.toString()?:"")
    body.add(ship?.shipInfo?.fullName?:"")
    body.add(ship?.shipInfo?.type?:"")
    body.add((ship as? ShipDto)?.cond?.toString()?:"")
    body.add((ship as? ShipDto)?.nowhp?.toString()?:"")
    body.add(maxHp.toString())
    val sonsyo =
            (ship as? ShipDto)?.run {
                val hpRate = 4.0 * this.nowhp.toDouble() / maxHp.toDouble()
                if (hpRate > 3) { "小破未満" }
                else if (hpRate > 2) { "小破" }
                else if (hpRate > 1) { "中破" }
                else if (hpRate > 0) { "大破" }
                else { "轟沈" }
            }?:""
    body.add(sonsyo)
    body.add(ship?.lv?.toString()?:"")
    body.add(ship?.param?.taisen?.toString()?:"")
    body.addAll(AkakariItemRowBody(ship,date))
    return body

}
