package logbook.builtinscript

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import logbook.builtinscript.akakariLog.AkakariSyutsugekiLogReader
import logbook.dto.BattleExDto
import logbook.dto.BattleAtackDto
import logbook.dto.ShipDto
import logbook.internal.Item
import logbook.scripting.BuiltinScriptFilter
import logbook.util.GsonUtil
import java.util.*

fun AkakariSienRowHeader(): ArrayList<String> {
    val header = DamageDayNightRowHeader()
    header.add("戦闘種別")
    header.add("支援種別")
    for (i in 1..6) {
        val index = i.toString()
        AkakariSienShipSummaryRowHeader()
                .forEach { s -> header.add("攻撃艦$index.$s") }
    }
    header.add("クリティカル")
    header.add("ダメージ")
    header.add("かばう")
    ShipRowHeader()
            .forEach { s -> header.add("防御艦." + s) }
    header.add("艦隊種類")
    header.add("敵艦隊種類")
    return header
}

private fun AkakariSienRowBodyConstruct(
        arg:ScriptArg,
        attackList: List<BattleAtackDto>?,
        apiName: String,
        phase:BattleExDto.Phase,
        startHP: ArrayList<IntArray>,
        body: ArrayList<ArrayList<String>>
) {
    if (attackList == null) {
        return
    }
    val prevHP = startHP
    val api_support_info = phase.tree?.get(apiName) as? LinkedTreeMap<*, *>
    if (api_support_info == null) {
        return
    }
    val api_support_hourai = api_support_info.get("api_support_hourai") as? LinkedTreeMap<*, *>
    if (api_support_hourai == null) {
        return
    }
    val rowHead = if(phase.isNight) DamageNightRowBody(arg) else DamageDayRowBody(arg)
    rowHead.add(if(phase.isNight) "夜戦" else "砲撃戦")

    val type = GsonUtil.toInt(phase.tree.get(if(phase.isNight) "api_n_support_flag" else "api_support_flag"))
    rowHead.add(if(type==2) "砲撃" else if (type == 3) "雷撃" else "不明")
    val shipIds = GsonUtil.toIntArray(api_support_hourai.get("api_ship_id"))
    val shipList = ArrayList<ShipDto>()
    if(arg.hasAkakariInfo){
        val log = AkakariSyutsugekiLogReader.battleDateToLog(arg.battle.battleDate)
        for(id in shipIds){
            if(id < 0){
                continue
            }
            val ship = log?.shipIdToStartShip(id.toString())
            ship?.run { shipList.add(this) }
        }
    }
    for(i in 0..5){
        val ship = shipList.tryGet(i)
        rowHead.addAll(AkakariSienShipSummaryRow(ship,ship?.maxhp?: -1))
    }

    val api_damage = GsonUtil.toDoubleArray(api_support_hourai.get("api_damage"))
    val api_cl_list = GsonUtil.toIntArray(api_support_hourai.get("api_cl_list"))
    val offset = if(api_cl_list[0] == -1) 1 else 0
    for(i in 0..5){
        val damage = api_damage.tryGet(i+offset) ?: 0.0
        val cl = api_cl_list.tryGet(i+offset) ?: 0
        val kabau = damage - damage.toInt().toDouble() > 0.05
        val row = ArrayList<String>(rowHead)
        row.add(cl.toString())
        row.add(damage.toInt().toString())
        row.add(if(kabau) "1" else "0")
        row.addAll(arg.enemyRows[i])
        row.add(if(arg.battle.isCombined) "連合艦隊" else "通常艦隊")
        row.add(if(arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
        if (arg.filter.filterOutput(row)) {
            body.add(row)
        }
    }
    if(arg.battle.isEnemyCombined){
        for(i in 0..5){
            val damage = api_damage.tryGet(i+offset+6) ?: 0.0
            val cl = api_cl_list.tryGet(i+offset+6) ?: 0
            val kabau = damage - damage.toInt().toDouble() > 0.05
            val row = ArrayList<String>(rowHead)
            row.add(cl.toString())
            row.add(damage.toInt().toString())
            row.add(if(kabau) "1" else "0")
            row.addAll(arg.enemyCombinedRows[i])
            row.add(if(arg.battle.isCombined) "連合艦隊" else "通常艦隊")
            row.add(if(arg.battle.isEnemyCombined) "連合艦隊" else "通常艦隊")
            if (arg.filter.filterOutput(row)) {
                body.add(row)
            }
        }
    }
}

fun AkakariSienRowBody(arg:ScriptArg): ArrayList<ArrayList<String>> {
    val body = ArrayList<ArrayList<String>>()
    arg.dayPhaseOrNull?.run {
        val phase = this
        AkakariSienRowBodyConstruct(
                arg = arg,
                apiName = "api_support_info",
                attackList = phase.support,
                phase = phase,
                startHP = arg.battleHP.dayPhase!!.supportStartHP,
                body = body
        )
    }
    arg.nightPhaseOrNull?.run {
        val phase = this
        AkakariSienRowBodyConstruct(
                arg = arg,
                apiName = "api_n_support_info",
                attackList = phase.support,
                phase = phase,
                startHP = arg.battleHP.nightPhase!!.supportStartHP,
                body = body
        )
    }
    return body
}