package logbook.builtinscript

import com.google.gson.internal.LinkedTreeMap
import logbook.scripting.BuiltinScriptFilter
import java.util.*
import java.util.stream.Collectors


fun HenseiRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("昼戦|夜戦")
    for (i in 1..6) {
        val index = i.toString()
        ShipRowHeader().forEach{ s -> header.add("自軍$index.$s") }
    }
    for (i in 1..6) {
        val index = i.toString()
        ShipSummaryRowHeader().forEach { s -> header.add("敵軍$index.$s") }
    }
    for (i in 1..6) {
        val index = i.toString()
        ShipRowHeader().forEach{ s -> header.add("自軍連合第二艦隊$index.$s") }
    }
    for (i in 1..6) {
        val index = i.toString()
        ShipSummaryRowHeader().forEach { s -> header.add("敵軍連合第二艦隊$index.$s") }
    }
    header.add("艦隊種類")
    header.add("敵艦隊種類")
    return header
}

fun HenseiRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.dayPhaseOrNull?.run {
        val row = ArrayList<String>()
        row.addAll(DamageDayRowBody(arg))
        row.add("昼戦")
        for (i in 0..5) {
            row.addAll(arg.friendRows[i].updateShipRowBody(arg.battleHP.dayPhase!!.dayPhaseStartHP[HP_INDEX_FRIEND][i], arg.battle.maxFriendHp?.tryGet(i) ?: -1))
        }
        for (i in 0..5) {
            row.addAll(arg.enemySummaryRows[i])
        }
        for (i in 0..5) {
            row.addAll(arg.combinedRows[i].updateShipRowBody(arg.battleHP.dayPhase!!.dayPhaseStartHP[HP_INDEX_FRIEND_COMBINED][i], arg.battle.maxFriendHpCombined?.tryGet(i) ?: -1))
        }
        for (i in 0..5) {
            row.addAll(arg.enemyCombinedSummaryRows[i])
        }
        row.add(arg.combinedFlagString)
        row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
        if (arg.filter.filterOutput(row)) {
            body.add(row)
        }
    }
    arg.nightPhaseOrNull?.run {
        val row = ArrayList<String>()
        row.addAll(DamageDayRowBody(arg))
        row.add("夜戦")
        for (i in 0..5) {
            row.addAll(arg.friendRows[i].updateShipRowBody(arg.battleHP.nightPhase!!.nightPhaseStartHP[HP_INDEX_FRIEND][i], arg.battle.maxFriendHp?.tryGet(i) ?: -1))
        }
        for (i in 0..5) {
            row.addAll(arg.enemySummaryRows[i])
        }
        for (i in 0..5) {
            row.addAll(arg.combinedRows[i].updateShipRowBody(arg.battleHP.nightPhase!!.nightPhaseStartHP[HP_INDEX_FRIEND_COMBINED][i], arg.battle.maxFriendHpCombined?.tryGet(i) ?: -1))
        }
        for (i in 0..5) {
            row.addAll(arg.enemyCombinedSummaryRows[i])
        }
        row.add(arg.combinedFlagString)
        row.add(if (arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
        if (arg.filter.filterOutput(row)) {
            body.add(row)
        }
    }
    return body
}
