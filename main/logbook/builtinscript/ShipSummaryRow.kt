package logbook.builtinscript

import logbook.dto.ShipBaseDto
import java.util.*

fun ShipSummaryRowHeader(): ArrayList<String> {
    val header = ArrayList<String>()
    header.add("ID")
    header.add("名前")
    header.add("Lv")
    return header
}
fun ShipSummaryRowBody(ship: ShipBaseDto?): ArrayList<String> {
    val body = ArrayList<String>()
    body.add(ship?.shipInfo?.shipId?.toString()?:"")
    body.add(ship?.shipInfo?.fullName?.toString()?:"")
    body.add(ship?.lv?.toString()?:"")
    return body
}